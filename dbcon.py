


import mysql.connector

def get_employees():
    conn = mysql.connector.connect(
        host="localhost",
        user="root",
        password="root",
        database="employez"
    )

    cursor = conn.cursor()
    cursor.execute("SELECT * FROM immig_details")
    

    rows = cursor.fetchall()

    #cursor.description auto-extracts column names from the query result
    columns =[desc[0] for desc in cursor.description]

    result = [dict(zip(columns, row)) for row in rows]

    cursor.close()
    conn.close()

    return result

    conn = get_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT * FROM immig_details")
    employees = cursor.fetchall()

    cursor.execute("SELECT * FROM departments")
    departments = cursor.fetchall()

    cursor.close()
    conn.close()