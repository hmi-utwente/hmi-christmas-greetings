from __future__ import print_function
import time
import sys
import getopt
from processSentence import *
from svmutil import *
import pickle
import random
import fileinput
import stomp
import json
import urllib
import subprocess

conn = {}
vocabList = {}
model = {}

class SpeechListener(stomp.ConnectionListener):
    def on_error(self, headers, message):
        print('received an error "%s"' % message)
    def on_message(self, headers, message):
	global conn, vocabList, model
        print('received a message "%s"' % message)
	raw = json.loads(message)['string']['input']
        trans = urllib.unquote_plus(raw.encode('utf-8')).decode('utf-8')
	words = trans.split(' ')
	if len(words) > 4:
	    sentenceFiltered = u' '.join(words)
	    print(sentenceFiltered)
	    x  = processSentence(sentenceFiltered, vocabList)
	    p_label, p_acc, p_val = svm_predict([1], [x], model, '-b 1 -q')
	    prob = p_val[0][0]
	    print(prob)
            conn.send({'destination': '/topic/twssOutput', 'body': '{ "score": '+str(prob)+' }'})

def main(argv=None):
    global conn, vocabList, model
    input = open('data/vocab.pk')
    vocabList = pickle.load(input)
    input.close()
    model = svm_load_model("data/svm_model.pk")
    conn = stomp.Connection()
    conn.set_listener('', SpeechListener())
    conn.start()
    conn.connect('admin', 'password', wait=True)
    conn.subscribe(destination='/topic/twssInput', id=1, ack='auto')
    print('setup done')
    raw_input()

if __name__ == "__main__":
    main()
    #sys.exit(main())
