const form = document.querySelector(".login-form");
const message = document.querySelector("[data-message]");

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    message.textContent = "Checking login...";

    const role = form.dataset.role;
    const next = form.dataset.next;
    const userId = new FormData(form).get("userId").trim();

    try {
        const employees = await requestJson("/employees");
        const user = employees.find((employee) => employee.employeeId.toLowerCase() === userId.toLowerCase());
        if (!user) {
            message.textContent = "ID not found in employee data.";
            return;
        }
        if (!isValidRole(role, user.employeeId)) {
            message.textContent = `This ID is not valid for ${role} login.`;
            return;
        }

        sessionStorage.setItem("employeeAlertUser", JSON.stringify({
            role,
            id: user.employeeId,
            name: user.employeeName
        }));
        window.location.href = next;
    } catch (error) {
        message.textContent = `Login failed: ${error.message}`;
    }
});

function isValidRole(role, id) {
    const normalized = id.toUpperCase();
    if (role === "employee") {
        return normalized.startsWith("EMP");
    }
    if (role === "manager") {
        return normalized.startsWith("MGR");
    }
    if (role === "hr") {
        return normalized.startsWith("HR");
    }
    return false;
}

async function requestJson(url) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`${response.status} ${response.statusText}`);
    }
    return response.json();
}
