from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, Response

from fastapi import UploadFile, File, Form
import shutil
import os
from datetime import date, datetime, timedelta
from urllib import error as urlerror
from urllib import request as urlrequest

# ✅ existing function (immigration)
from connection import get_employees  

# ✅ new function (employee_information)
from connection import (
    get_connection,
    get_employee_info,
    save_uploaded_file,
    get_employee_dashboard
)  

app = FastAPI()

UPLOAD_FOLDER = "uploads"
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DEMO1_BASE_URL = os.getenv("DEMO1_BASE_URL", "http://127.0.0.1:8081")

os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# ✅ CORS (for dashboard/frontend)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # for development
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ✅ Test route
@app.get("/")
def home(request: Request):
    accept = request.headers.get("accept", "")
    if "text/html" not in accept:
        return {"status": "working"}
    return FileResponse(os.path.join(BASE_DIR, "index.html"))


@app.get("/status")
def status():
    return {"status": "working"}


@app.get("/index.html")
def immigration_page():
    return FileResponse(os.path.join(BASE_DIR, "index.html"))


@app.get("/workauthorization.html")
def workauthorization_page():
    return FileResponse(os.path.join(BASE_DIR, "workauthorization.html"))


@app.get("/employee.html")
def employee_page():
    return FileResponse(os.path.join(BASE_DIR, "employee.html"))


# ✅ Existing API (already used in your dashboard)
@app.get("/immigration")
def immigration():
    return {"employees": get_employees()}


# ✅ NEW API (Work Authorization / employee_information)
@app.get("/workauthorization")
def workauthorization():
    return get_employee_info()

# ✅ Employee Dashboard API
@app.get("/employees")
def employees():
    return get_employee_dashboard()

# ✅ Optional: Combined API (both in one response)
@app.get("/dashboard")
def dashboard():
    return {
        "immigration": get_employees(),
        "employee_info": get_employee_info()
    }    

@app.post("/upload-file")
async def upload_file(
    emp_id: str = Form(...),
    file: UploadFile = File(...)
):

    file_path = os.path.join(
        UPLOAD_FOLDER,
        file.filename
    )

    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(
            file.file,
            buffer
        )

        save_uploaded_file(
        emp_id,
        file.filename,
        file_path
    )

    return {
        "message": "File uploaded successfully",
        "file_name": file.filename
    }


def parse_employee_date(value):
    if value is None:
        return None

    if isinstance(value, date):
        return value

    normalized = str(value).strip()
    if not normalized:
        return None

    for date_format in ("%Y-%m-%d", "%m/%d/%Y", "%d/%m/%Y"):
        try:
            return datetime.strptime(normalized, date_format).date()
        except ValueError:
            continue

    return None


def event_date_for_year(source_date, year):
    if source_date is None:
        return None

    try:
        return source_date.replace(year=year)
    except ValueError:
        return date(year, 2, 28)


def employee_name(employee):
    parts = [
        employee.get("FirstName"),
        employee.get("MiddleName"),
        employee.get("LastName"),
    ]
    return " ".join(part.strip() for part in parts if part and part.strip())


def load_report_employees():
    conn = get_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("""
        SELECT
            Emp_Id,
            FirstName,
            MiddleName,
            LastName,
            Email,
            DOB,
            HiringDate,
            Approver_Id,
            HR_Id
        FROM employee_information
        WHERE COALESCE(ActiveFlag, 1) = 1
    """)

    rows = cursor.fetchall()
    cursor.close()
    conn.close()

    employees_by_id = {row["Emp_Id"]: row for row in rows}

    result = []
    for row in rows:
        manager = employees_by_id.get(row.get("Approver_Id"))
        hr = employees_by_id.get(row.get("HR_Id"))
        dob = parse_employee_date(row.get("DOB"))
        hiring_date = parse_employee_date(row.get("HiringDate"))

        result.append({
            "employeeId": row.get("Emp_Id"),
            "employeeName": employee_name(row),
            "email": row.get("Email"),
            "dateOfBirth": dob.isoformat() if dob else None,
            "hiringDate": hiring_date.isoformat() if hiring_date else None,
            "managerId": row.get("Approver_Id"),
            "managerName": employee_name(manager) if manager else None,
            "hrId": row.get("HR_Id"),
            "hrName": employee_name(hr) if hr else None,
        })

    return result


def weekly_employee_events(reference_date):
    week_start = reference_date - timedelta(days=reference_date.weekday())
    week_end = week_start + timedelta(days=6)
    employees = load_report_employees()
    employees_by_id = {employee["employeeId"]: employee for employee in employees}
    events = []

    for employee in employees:
        dob = parse_employee_date(employee.get("dateOfBirth"))
        birthday = event_date_for_year(dob, week_start.year)
        if birthday and week_start <= birthday <= week_end:
            events.append(employee_event(employee, employees_by_id, "BIRTHDAY", birthday, None))

        hiring_date = parse_employee_date(employee.get("hiringDate"))
        anniversary = event_date_for_year(hiring_date, week_start.year)
        if anniversary and hiring_date and hiring_date < anniversary and week_start <= anniversary <= week_end:
            events.append(employee_event(
                employee,
                employees_by_id,
                "WORK_ANNIVERSARY",
                anniversary,
                anniversary.year - hiring_date.year,
            ))

    return events


def employee_event(employee, employees_by_id, event_type, event_date, anniversary_years):
    manager = employees_by_id.get(employee.get("managerId"))
    hr = employees_by_id.get(employee.get("hrId"))
    return {
        "employeeId": employee.get("employeeId"),
        "employeeName": employee.get("employeeName"),
        "employeeEmail": employee.get("email"),
        "eventType": event_type,
        "eventDate": event_date.isoformat(),
        "anniversaryYears": anniversary_years,
        "approverId": employee.get("managerId"),
        "approverName": manager.get("employeeName") if manager else employee.get("managerName"),
        "approverEmail": manager.get("email") if manager else None,
        "hrId": employee.get("hrId"),
        "hrName": hr.get("employeeName") if hr else employee.get("hrName"),
        "hrEmail": hr.get("email") if hr else None,
    }


@app.get("/demo1/employees")
def demo1_employees():
    return load_report_employees()


@app.get("/demo1/employees/current-week-events")
def demo1_current_week_events(date: str | None = None):
    reference_date = parse_employee_date(date) if date else datetime.now().date()
    return weekly_employee_events(reference_date or datetime.now().date())


@app.post("/demo1/employees/current-week-events/email")
def demo1_current_week_events_email(date: str | None = None):
    events = demo1_current_week_events(date)
    return [
        {
            "employeeId": event["employeeId"],
            "employeeName": event["employeeName"],
            "sent": False,
            "message": "Email sending is handled by the Spring demo1 service. The Python app loaded the event data only.",
        }
        for event in events
    ]


@app.get("/demo1/employee-alerts")
def demo1_employee_alerts(date: str | None = None):
    reference_date = parse_employee_date(date) if date else datetime.now().date()
    return alerts_for_events(weekly_employee_events(reference_date or datetime.now().date()))


@app.post("/demo1/employee-alerts/run")
def demo1_run_employee_alerts(date: str | None = None):
    return demo1_employee_alerts(date)


def alerts_for_events(events):
    return [
        {
            "id": index + 1,
            "employeeId": event["employeeId"],
            "alertType": event["eventType"],
            "alertDate": event["eventDate"],
            "sentAt": None,
            "status": "generated",
        }
        for index, event in enumerate(events)
    ]


@app.api_route("/demo1/{path:path}", methods=["GET", "POST", "PUT", "PATCH", "DELETE"])
async def demo1_proxy(path: str, request: Request):
    target_url = f"{DEMO1_BASE_URL.rstrip('/')}/{path}"
    if request.url.query:
        target_url = f"{target_url}?{request.url.query}"

    body = await request.body()
    headers = {
        key: value
        for key, value in request.headers.items()
        if key.lower() not in {"host", "content-length", "connection"}
    }

    proxy_request = urlrequest.Request(
        target_url,
        data=body if request.method not in {"GET", "HEAD"} else None,
        headers=headers,
        method=request.method,
    )

    try:
        with urlrequest.urlopen(proxy_request, timeout=30) as proxy_response:
            return Response(
                content=proxy_response.read(),
                status_code=proxy_response.status,
                media_type=proxy_response.headers.get("Content-Type"),
            )
    except urlerror.HTTPError as exc:
        return Response(
            content=exc.read(),
            status_code=exc.code,
            media_type=exc.headers.get("Content-Type"),
        )
    except urlerror.URLError as exc:
        return Response(
            content=f"Could not connect to demo1 service at {DEMO1_BASE_URL}: {exc.reason}",
            status_code=502,
            media_type="text/plain",
        )
