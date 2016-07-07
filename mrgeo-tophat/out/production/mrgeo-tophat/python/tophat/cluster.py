import json

import boto3
import datetime

from datetime import timedelta


def _response_ok(response):
    if response and 'ResponseMetadata' in response:
        if 'HTTPStatusCode' in response['ResponseMetadata']:
            if response['ResponseMetadata']['HTTPStatusCode'] != 200:
                raise Exception('AWS returned a bad response (http code ' + str(
                    response['ResponseMetadata']['HTTPStatusCode']) + ')')
    else:
        raise Exception("AWS returned a bad response")

def _json_serial(obj):
    """JSON serializer for objects not serializable by default json code"""

    if isinstance(obj, datetime.datetime):
        return obj.isoformat()
    raise TypeError ("Type not serializable")


def initialize():
    pass


def list_clusters(state=None, time=None, daysback=360):
    client = boto3.client('emr')

    if not state:
        st = "all"
    else:
        st = str(state).lower()

    states = []

    if st == 'all' or st == 'starting':
        states.append('STARTING')
    if st == 'all' or st == 'bootstrapping':
        states.append('BOOTSTRAPPING')
    if st == 'all' or st == 'running':
        states.append('RUNNING')
    if st == 'all' or st == 'waiting':
        states.append('WAITING')
    if st == 'all' or st == 'terminating':
        states.append('TERMINATING')
    if st == 'all' or st == 'terminated':
        states.append('TERMINATED')

    if not time:
        endtime = datetime.datetime.now()
    else:
        endtime = time

    starttime = endtime - timedelta(days=daysback)
    marker = None

    clusters = {}
    while True:
        if marker:
            response = client.list_clusters(ClusterStates=states, CreatedAfter=starttime, CreatedBefore=endtime,
                                            Marker=marker)
        else:
            response = client.list_clusters(ClusterStates=states, CreatedAfter=starttime, CreatedBefore=endtime)

        try:
            _response_ok(response)
        except Exception, e:
            raise Exception(e.message)

        if "Clusters" in response:
            for cluster in response['Clusters']:
                id = cluster['Id']

                response = client.describe_cluster(ClusterId=id)
                try:
                    _response_ok(response)
                except Exception, e:
                    raise Exception(e.message)

                # print(json.dumps(response, default=_json_serial, indent=2, sort_keys=True))

                if 'Cluster' in response:
                    dc = response['Cluster']

                    if 'Tags' in dc:
                        tags = dc['Tags']
                        tophat_cluster = {}
                        for tag in tags:
                            # REMOVE ME when clusters are really tagged!
                            tag['Key'] = 'tophat.' + tag['Key']
                            if tag['Key'].startswith('tophat.'):
                                tophat_cluster[tag['Key'][len('tophat.'):]] = tag['Value']

                        if len(tophat_cluster) > 0:
                            status = dc['Status']
                            state = {'state': status['State']}
                            if 'StateChangeReason' in status:
                                if 'Code' in status['StateChangeReason']:
                                    state['code'] = status['StateChangeReason']['Code']
                                if 'Message' in status['StateChangeReason']:
                                    state['message'] = status['StateChangeReason']['Message']

                            if status['State'] != "TERMINATED":
                                master = dc['MasterPublicDnsName']
                            else:
                                master = ''

                            cl = {'name': cluster['Name'], 'id': id, 'state': state, "log": dc['LogUri'], 'master': master}

                            for k, v in tophat_cluster.iteritems():
                                cl[k] = v

                            clusters[cl['name']] = cl

        if 'Marker' not in response:
            break

        marker = response['Marker']

    return clusters


def create_cluster(cluster_template=None, json=None):
    pass


class Cluster(object):
    def status(self):
        pass

    def attach(self):
        pass

    def detach(self):
        pass

    def destroy(self):
        pass


print(json.dumps(list_clusters(), default=_json_serial, indent=2, sort_keys=True))
