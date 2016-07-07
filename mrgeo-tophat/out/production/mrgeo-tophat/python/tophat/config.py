# Server settings
SERVER_NAME = "localhost:8888"

STANDALONE = True

# Flask settings
SECRET_KEY = 'THIS IS AN INSECURE SECRET'
SQLALCHEMY_DATABASE_URI = 'sqlite:///tophat.sqlite'
CSRF_ENABLED = True

# Flask-Mail parameters
MAIL_USERNAME = 'email@example.com'
MAIL_PASSWORD = 'password'
MAIL_DEFAULT_SENDER = '"Sender" <noreply@example.com>'
MAIL_SERVER = 'smtp.gmail.com'
MAIL_PORT = 465
MAIL_USE_SSL = True
MAIL_USE_TLS = False

# Flask-User settings
USER_APP_NAME = 'tophat'

USER_ENABLE_CHANGE_PASSWORD = True  # Allow users to change their password
USER_ENABLE_CHANGE_USERNAME = True  # Allow users to change their username. Requires USER_ENABLE_USERNAME=True
USER_ENABLE_CONFIRM_EMAIL = False # True  # Force users to confirm their email. Requires USER_ENABLE_EMAIL=True
USER_ENABLE_FORGOT_PASSWORD = False # True  # Allow users to reset their passwords. Requires USER_ENABLE_EMAIL=True
USER_ENABLE_LOGIN_WITHOUT_CONFIRM = False  # Allow users to login without a confirmed email address, Protect views using @confirm_email_required
USER_ENABLE_EMAIL = True  # Register with Email. Requires USER_ENABLE_REGISTRATION=True
USER_ENABLE_MULTIPLE_EMAILS = False  # Users may register multiple emails. Requires USER_ENABLE_EMAIL=True

USER_ENABLE_REGISTRATION = True  # Allow new users to register

USER_ENABLE_RETYPE_PASSWORD = True  # Prompt for `retype password` in:
#   - registration form,
#   - change password form, and
#   - reset password forms.

USER_ENABLE_USERNAME = True  # Register and Login with username

USER_PASSWORD_HASH = 'sha512_crypt' # 'bcrypt'
