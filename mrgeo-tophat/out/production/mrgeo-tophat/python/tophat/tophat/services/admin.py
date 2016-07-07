from flask_user import login_required, roles_required

from tophat import app


@login_required
@roles_required("admin")
@app.route('/admin')
def admin():
    return "admin"
