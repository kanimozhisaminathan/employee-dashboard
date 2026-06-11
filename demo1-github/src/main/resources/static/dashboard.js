const role = document.body.dataset.dashboardRole;
const storedUser = sessionStorage.getItem("employeeAlertUser");
const user = storedUser ? JSON.parse(storedUser) : null;

if (!user || user.role !== role) {
    window.location.href = `/${role}-login.html`;
}

const state = {
    events: [],
    visibleEvents: [],
    results: []
};

const referenceDate = document.querySelector("#referenceDate");
const loadEvents = document.querySelector("#loadEvents");
const sendEmails = document.querySelector("#sendEmails");
const eventsTable = document.querySelector("#eventsTable");
const emptyState = document.querySelector("#emptyState");
const weekLabel = document.querySelector("#weekLabel");
const totalEvents = document.querySelector("#totalEvents");
const birthdayCount = document.querySelector("#birthdayCount");
const anniversaryCount = document.querySelector("#anniversaryCount");
const sentCount = document.querySelector("#sentCount");
const emailSummary = document.querySelector("#emailSummary");
const emailResults = document.querySelector("#emailResults");
const userBadge = document.querySelector("#userBadge");
const logout = document.querySelector("#logout");

referenceDate.value = new Date().toISOString().slice(0, 10);
userBadge.textContent = `${user.name} (${user.id})`;

loadEvents.addEventListener("click", () => fetchEvents());
logout.addEventListener("click", () => {
    sessionStorage.removeItem("employeeAlertUser");
    window.location.href = "/";
});

if (sendEmails) {
    sendEmails.addEventListener("click", () => sendEmailsForVisibleEvents());
}

fetchEvents();

async function fetchEvents() {
    setBusy(true);
    clearEmailResults();

    try {
        state.events = await requestJson(`/employees/current-week-events?date=${referenceDate.value}`);
        state.visibleEvents = filterEvents(state.events);
        renderEvents();
    } catch (error) {
        emptyState.style.display = "block";
        emptyState.textContent = error.message;
    } finally {
        setBusy(false);
    }
}

async function sendEmailsForVisibleEvents() {
    setBusy(true);
    try {
        const allResults = await requestJson(`/employees/current-week-events/email?date=${referenceDate.value}`, {
            method: "POST"
        });
        const visibleKeys = new Set(state.visibleEvents.map(eventKey));
        state.results = allResults.filter((result) => visibleKeys.has(resultKey(result)));
        renderEmailResults();
    } catch (error) {
        emailResults.innerHTML = resultMarkup(false, "Request failed", error.message);
    } finally {
        setBusy(false);
    }
}

function filterEvents(events) {
    if (role === "employee") {
        return events.filter((event) => event.employeeId === user.id);
    }
    if (role === "manager") {
        return events.filter((event) => event.approverId === user.id);
    }
    if (role === "hr") {
        return events.filter((event) => event.hrId === user.id);
    }
    return events;
}

function renderEvents() {
    const birthdays = state.visibleEvents.filter((event) => event.eventType === "BIRTHDAY").length;
    const anniversaries = state.visibleEvents.filter((event) => event.eventType === "WORK_ANNIVERSARY").length;

    totalEvents.textContent = state.visibleEvents.length;
    birthdayCount.textContent = birthdays;
    anniversaryCount.textContent = anniversaries;
    weekLabel.textContent = `Reference date: ${referenceDate.value}`;
    emptyState.style.display = state.visibleEvents.length ? "none" : "block";
    emptyState.textContent = "No events found for this login.";

    if (sendEmails) {
        sendEmails.disabled = state.visibleEvents.length === 0;
    }

    eventsTable.innerHTML = state.visibleEvents.map((event) => `
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
    if (sentCount) {
        sentCount.textContent = sent;
    }
    emailSummary.textContent = `${sent} of ${state.results.length} sent`;
    emailResults.innerHTML = state.results.map((result) => resultMarkup(
        result.sent,
        `${result.employeeName} - ${labelFor(result.eventType)}`,
        result.message
    )).join("");
}

function clearEmailResults() {
    state.results = [];
    if (sentCount) {
        sentCount.textContent = "0";
    }
    if (emailSummary) {
        emailSummary.textContent = "";
    }
    if (emailResults) {
        emailResults.innerHTML = "";
    }
}

function eventKey(event) {
    return `${event.employeeId}|${event.eventType}`;
}

function resultKey(result) {
    return `${result.employeeId}|${result.eventType}`;
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        throw new Error(`${response.status} ${response.statusText}`);
    }
    return response.json();
}

function setBusy(isBusy) {
    loadEvents.disabled = isBusy;
    if (sendEmails) {
        sendEmails.disabled = isBusy || state.visibleEvents.length === 0;
    }
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

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
