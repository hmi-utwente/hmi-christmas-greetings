import stomp
import json
import urllib
import sys
import getopt


def main(argv=None):
    topic = "/topic/Random"
    msg = "{}"
    opts, rem = getopt.getopt(sys.argv[1:],"t:")
    for opt, arg in opts:
	if opt in ('-t'):
	    topic = arg
	else:
	    sys.exit(2)

    conn = stomp.Connection([('192.168.1.149', 61613)])
    conn.start()
    conn.connect('admin', 'password', wait=True)
    print("Sending "+rem[0]+" to "+str(topic))
    conn.send(topic, rem[0])
    sys.exit(0)


if __name__ == "__main__":
    main()
    #sys.exit(main())
