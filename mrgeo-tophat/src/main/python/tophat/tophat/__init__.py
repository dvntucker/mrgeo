from flask import Flask

app = Flask(__name__)
app.config.from_object('config')


import services.cluster
import services.index
import services.user
import services.admin