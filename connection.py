import mysql.connector


# 🔹 Common connection function (reuse)
def get_connection():
    return mysql.connector.connect(
        host="localhost",
        user="root",
        password="root",
        database="employez"
    )


# 🔹 1. Immigration Data (immig_details)
def get_employees():
    conn = get_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT * FROM immig_details")
    rows = cursor.fetchall()

    columns = [desc[0] for desc in cursor.description]
    result = [dict(zip(columns, row)) for row in rows]

    cursor.close()
    conn.close()

    return result


# 🔹 2. Work Authorization (employee_information)
def get_employee_info():
    conn = get_connection()
    cursor = conn.cursor()

    query = """
    SELECT Emp_id, FirstName, VisaType, VisaExpDate, IdType, IdNumber, IdExpDate
    FROM employee_information
    """

    cursor.execute(query)
    rows = cursor.fetchall()

    columns = [desc[0] for desc in cursor.description]
    result = [dict(zip(columns, row)) for row in rows]

    cursor.close()
    conn.close()

    return result  


def save_uploaded_file(emp_id, file_name, file_path):
    conn = get_connection()
    cursor = conn.cursor()

    query = """
    INSERT INTO work_authorization_files
    (emp_id, file_name, file_path)
    VALUES (%s, %s, %s)
    """

    cursor.execute(
        query,
        (emp_id, file_name, file_path)
    )

    conn.commit()

    cursor.close()
    conn.close()

# 🔹 3. Employee Data
def get_employee_dashboard():
    conn = get_connection()
    cursor = conn.cursor()

    query = """
    SELECT 
        Emp_id,
        FirstName,
        VisaType,
        VisaExpDate,
        IdType,
        IdNumber
    FROM employee_information
    """

    cursor.execute(query)

    rows = cursor.fetchall()

    columns = [desc[0] for desc in cursor.description]

    result = [dict(zip(columns, row)) for row in rows]

    cursor.close()
    conn.close()

    return result    