const state = {
    events: [],
    results: []
};

const referenceDate = document.querySelector("#referenceDate");
const loadEvents = document.querySelector("#loadEvents");
const sendEmails = document.querySelector("#sendEmails");
const eventsTable = document.querySelector("#eventsTable");
const emptyState = document.querySelector("#emptyState");
const appStatus = document.querySelector("#appStatus");
const weekLabel = document.querySelector("#weekLabel");
const emailSummary = document.querySelector("#emailSummary");
const emailResults = document.querySelector("#emailResults");
const totalEvents = document.querySelector("#totalEvents");
const birthdayCount = document.querySelector("#birthdayCount");
const anniversaryCount = document.querySelector("#anniversaryCount");
const sentCount = document.querySelector("#sentCount");

referenceDate.value = new Date().toISOString().slice(0, 10);

loadEvents.addEventListener("click", () => fetchEvents());
sendEmails.addEventListener("click", () => sendCurrentWeekEmails());

fetchEvents();

async function fetchEvents() {
    setBusy(true, "Loading");
    clearEmailResults();

    try {
        const data = await requestJson(`/employees/current-week-events?date=${referenceDate.value}`);
        state.events = data;
        renderEvents();
        setStatus("Ready");
    } catch (error) {
        setStatus("Load failed");
        showError(error.message);
    } finally {
        setBusy(false);
    }
}

async function sendCurrentWeekEmails() {
    setBusy(true, "Sending");

    try {
        const data = await requestJson(`/employees/current-week-events/email?date=${referenceDate.value}`, {
            method: "POST"
        });
        state.results = data;
        renderEmailResults();
        setStatus(data.every((item) => item.sent) ? "Sent" : "Check results");
    } catch (error) {
        setStatus("Send failed");
        emailResults.innerHTML = resultMarkup(false, "Request failed", error.message);
    } finally {
        setBusy(false);
    }
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        throw new Error(`${response.status} ${response.statusText}`);
    }
    return response.json();
}

function renderEvents() {
    const birthdays = state.events.filter((event) => event.eventType === "BIRTHDAY").length;
    const anniversaries = state.events.filter((event) => event.eventType === "WORK_ANNIVERSARY").length;

    totalEvents.textContent = state.events.length;
    birthdayCount.textContent = birthdays;
    anniversaryCount.textContent = anniversaries;
    weekLabel.textContent = `Reference date: ${referenceDate.value}`;
    emptyState.style.display = state.events.length ? "none" : "block";
    sendEmails.disabled = state.events.length === 0;

    eventsTable.innerHTML = state.events.map((event) => `
        <tr>
            <td><strong>${escapeHtml(event.employeeName)}</strong><br>${escapeHtml(event.employeeId)}</td>
            <td>${escapeHtml(event.employeeEmail || "")}</td>
            <td>${eventTag(event.eventType)}</td>
            <td>${escapeHtml(event.eventDate)}</td>
            <td>${escapeHtml(event.approverName || "")}<br>${escapeHtml(event.approverId || "")}<br>${escapeHtml(event.approverEmail || "")}</td>
            <td>${escapeHtml(event.hrName || "")}<br>${escapeHtml(event.hrId || "")}<br>${escapeHtml(event.hrEmail || "")}</td>
        </tr>
    `).join("");
}

function renderEmailResults() {
    const sent = state.results.filter((result) => result.sent).length;
    sentCount.textContent = sent;
    emailSummary.textContent = `${sent} of ${state.results.length} sent`;
    emailResults.innerHTML = state.results.map((result) => resultMarkup(
        result.sent,
        `${result.employeeName} - ${labelFor(result.eventType)}`,
        result.message
    )).join("");
}

function clearEmailResults() {
    state.results = [];
    sentCount.textContent = "0";
    emailSummary.textContent = "";
    emailResults.innerHTML = "";
}

function resultMarkup(sent, title, message) {
    return `
        <div class="result ${sent ? "sent" : "failed"}">
            <strong>${escapeHtml(sent ? "Sent" : "Failed")} - ${escapeHtml(title)}</strong>
            <p>${escapeHtml(message || "")}</p>
        </div>
    `;
}

function eventTag(type) {
    const isAnniversary = type === "WORK_ANNIVERSARY";
    return `<span class="tag ${isAnniversary ? "anniversary" : ""}">${labelFor(type)}</span>`;
}

function labelFor(type) {
    return type === "WORK_ANNIVERSARY" ? "Work Anniversary" : "Birthday";
}

function setBusy(isBusy, label = "Ready") {
    loadEvents.disabled = isBusy;
    sendEmails.disabled = isBusy || state.events.length === 0;
    if (isBusy) {
        setStatus(label);
    }
}

function setStatus(text) {
    appStatus.textContent = text;
}

function showError(message) {
    emptyState.style.display = "block";
    emptyState.textContent = message;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
