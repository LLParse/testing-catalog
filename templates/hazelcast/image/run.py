#!/usr/bin/env python

from jinja2 import Template, Environment, FileSystemLoader
from subprocess import call
from urllib2 import urlopen, HTTPError, URLError
from argparse import ArgumentParser

parser = ArgumentParser(description='Run hazelcast components')
parser.add_argument('--mancenter', action='store_true')
args = parser.parse_args()

# interface we bind to
def get_interface():
    interface='127.0.0.1'
    try:
        interface=urlopen('http://rancher-metadata/2015-12-19/self/container/ips/0').read()
    except URLError:
        print "WARNING: Using default interface {0}".format(interface)
    except HTTPError as e:
        print "WARNING: {0}! Using default interface {1}".format(e.strerror, interface)
    return interface

# initial cluster members
def get_members():
    members=['127.0.0.1:5701']
    try:
        containers=urlopen('http://rancher-metadata/2015-12-19/self/service/containers').read()
        members=[]
        for container in containers.split('\n'):
            if container:
                members+=[container.split('=')[-1]+':5701',]
    except URLError:
        print "WARNING: Using default members {0}".format(members)
    except HTTPError as e:
        print "WARNING: {0}! Using default members {1}".format(e.strerror, members)
    return members

if args.mancenter:
    call(['java', '-jar', '/hazelcast/mancenter/mancenter-3.6.2.war', '8080', '/'])
else:
    env = Environment(loader=FileSystemLoader('/'))
    template = env.get_template('config.xml.j2')

    with open('/config.xml', 'w') as f:
        f.write(template.render(
            name='dev',
            password='dev-pass',
            interface=get_interface(),
            members=get_members(),
            # Number of backups. If 1 is set as the backup-count for example,
            # then all entries of the map will be copied to another JVM for
            # fail-safety. 0 means no backup.
            backup_count=1
        ))

    call(['java', '-cp', "/hazelcast/lib/hazelcast-all-3.6.2.jar", '-server', '-Dhazelcast.config=/config.xml', 'com.hazelcast.core.server.StartServer'])
