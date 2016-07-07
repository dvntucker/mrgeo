from tophat import app


@app.route('/')
def index():
    return 'Hello TopHat!'
