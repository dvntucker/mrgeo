from flask_login import logout_user
from flask_user import login_required

from tophat import app

# @app.route('/session/<user_id>')
# @login_required
# def session(user_id):
#     print "Getting session key for " + user_id
#     response = {'id': user_id, 'key': str(uuid.uuid4())}
#     return json.dumps(response, indent=2, sort_keys=True)


@app.route('/logout')
def logout():
    logout_user()
    return "user logged out"


@app.route('/profile')
@login_required
def profile():
    return "profile"




