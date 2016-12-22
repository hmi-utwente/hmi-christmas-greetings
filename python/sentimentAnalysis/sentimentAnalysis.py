from __future__ import print_function
import time
import sys
import getopt
import stomp
import json
import urllib
from textblob import TextBlob



conn = {}

class SpeechListener(stomp.ConnectionListener):
    def on_error(self, headers, message):
        print('received an error "%s"' % message)
    def on_message(self, headers, message):
	global conn
        print('received a message "%s"' % message)
	raw = json.loads(message)['sentence']
        trans = TextBlob(urllib.unquote_plus(raw.encode('utf-8')).decode('utf-8'))
        print(str(trans.sentiment.polarity)+" "+str(trans.sentiment.subjectivity))
        conn.send('/topic/sentimentOutput', '{ "polarity": '+str(trans.sentiment.polarity)+', "subjectivity": '+str(trans.sentiment.subjectivity)+' }')

def main(argv=None):
    global conn
    conn = stomp.Connection([('192.168.1.149', 61613)])
    conn.set_listener('', SpeechListener())
    conn.start()
    conn.connect('admin', 'password', wait=True)
    conn.subscribe(destination='/topic/sentimentInput', id=1, ack='auto')
    print('setup done')
    raw_input()

if __name__ == "__main__":
    main()
    #sys.exit(main())
