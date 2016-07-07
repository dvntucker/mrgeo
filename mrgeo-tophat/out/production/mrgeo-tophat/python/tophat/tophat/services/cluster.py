from flask import render_template
from flask_user import login_required, roles_required

from tophat import app


@login_required
@roles_required("create")
@app.route("/cluster/create")
def create():
    return "create"


@login_required
@app.route("/cluster/list")
def list_clusters():
    if app.config['STANDALONE']:
        return render_template('standalone/cluster_list.html')
    return "list"


@login_required
@app.route("/cluster/status/<cluster_id>")
def status(cluster_id):
    return "status"


@login_required
@app.route("/cluster/destroy/<cluster_id>")
def destroy(cluster_id):
    return "destroy"


@login_required
@app.route("/cluster/attach<cluster_id>")
def attach(cluster_id):
    return "attach"


@login_required
@app.route("/cluster/detach/<cluster_id>")
def detach(cluster_id):
    return "detach"
