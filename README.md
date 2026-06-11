Employee Dashboard — HR Management System

A web-based HR management dashboard built with Python FastAPI and vanilla JavaScript for tracking employee immigration status, work authorization documents, and birthday/anniversary alerts.


Features

FeatureDescriptionImmigration DashboardTrack USCIS receipts, I-94, I-140, LCA amounts, visa expiry and Green Card statusWork AuthorizationMonitor visa types, ID documents, expiry dates and upload files per employeeBirthday & Anniversary AlertsAuto-detects employees with birthdays or work anniversaries in the current weekVisual ChartsPie charts showing immigration status and visa type distributionFile UploadUpload and store work authorization documents linked to each employee


Tech Stack

LayerTechnologyBackendPython, FastAPI, UvicornDatabaseMySQLDB Connectormysql-connector-pythonFrontendHTML, CSS, JavaScript (Vanilla)ChartsChart.jsUI FrameworkBootstrap 5


Project Structure

employee-dashboard/
├── main.py                    # FastAPI app — all API routes
├── connection.py              # Database connection and queries
├── dbcon.py                   # Earlier draft of DB connection
├── dbmain.py                  # Earlier draft of FastAPI app
├── index.html                 # Immigration dashboard page
├── workauthorization.html     # Work authorization page
├── employee.html              # Birthday and anniversary alerts page
├── start-reports-app.ps1      # PowerShell script to start the server
└── uploads/                   # Uploaded employee documents (auto-created)


Setup and Installation

Step 1 — Clone the repository

bashgit clone https://github.com/kanimozhisaminathan/employee-dashboard.git
cd employee-dashboard

Step 2 — Install Python dependencies

bashpip install fastapi uvicorn mysql-connector-python python-dotenv

Step 3 — Set up MySQL database


Create a database named employez
Create the required tables: immig_details, employee_information, work_authorization_files


Step 4 — Configure database credentials

Create a .env file in the root folder:

DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=employez

Step 5 — Run the server

bashuvicorn main:app --reload --port 8000

Step 6 — Open in browser

http://localhost:8000


API Endpoints

MethodEndpointDescriptionGET/Serves the immigration dashboardGET/immigrationReturns all immigration recordsGET/workauthorizationReturns work authorization dataGET/employeesReturns employee dashboard dataPOST/upload-fileUploads a document for an employeeGET/demo1/employees/current-week-eventsReturns this week's birthdays and anniversaries


Pages Overview

Immigration page — index.html


Summary cards: total records, average LCA, max LCA, total LCA
Pie chart showing Active / Expired / Expiring Soon breakdown
Full immigration data table with all employee records


Work Authorization page — workauthorization.html


Total employee count summary card
Pie chart showing visa type distribution
Employee table with visa details, ID info and per-employee file upload


Employee Alerts page — employee.html


Weekly birthday and work anniversary alerts
Shows employee name, event date, years of service
Includes manager and HR contact details for each alert


﻿
