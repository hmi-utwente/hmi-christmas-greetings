from __future__ import print_function
from googleapiclient.discovery import build
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
service = {}
vocabList = {}
model = {}

lastSend = 0

class SpeechListener(stomp.ConnectionListener):
    def on_error(self, headers, message):
        print('received an error "%s"' % message)
    def on_message(self, headers, message):
	global service, vocabList, model, lastSend
        print('received a message "%s"' % message)
	raw = json.loads(message)['string']['content']
        trans = urllib.unquote_plus(raw.encode('utf-8')).decode('utf-8')
	words = trans.split(' ')
	wordsFiltered = [ x for x in words if "<unk>" not in x ]
	if len(wordsFiltered) > 5:
            wordsFiltered = wordsFiltered[-10:]
	    sentenceFiltered = u' '.join(wordsFiltered)
	    print(sentenceFiltered)
	    res = service.translations().list(
		source='nl',
		target='en',
		q=[sentenceFiltered]).execute()
	    sentenceTranslated = res['translations'][0]['translatedText']
	    print(sentenceTranslated)
	    x  = processSentence(sentenceTranslated, vocabList)
	    p_label, p_acc, p_val = svm_predict([1], [x], model, '-b 1 -q')
	    prob = p_val[0][0]
	    print(prob)
	    timeNow = int(round(time.time()))
	    timeDelta = timeNow-lastSend
	    if timeDelta > 15 and prob > 0.4:
	        lastSend = timeNow
		print("THAT IS WHAT SHE SAID!")
    		subprocess.call(["say", "Thats what she said!"])
	    

    

def twss(sentence,vocabList,model):
    x  = processSentence(sentence, vocabList)
    p_label, p_acc, p_val = svm_predict([1], [x], model, '-b 1 -q')
    return p_val[0][0]

def main(argv=None):
    subprocess.call(["say", "Listening now!"])
    global conn, service, vocabList, model
    input = open('data/vocab.pk')
    vocabList = pickle.load(input)
    input.close()
    model = svm_load_model("data/svm_model.pk")
    conn = stomp.Connection()
    conn.set_listener('', SpeechListener())
    conn.start()
    conn.connect('admin', 'password', wait=True)
    conn.subscribe(destination='/topic/speechLive', id=1, ack='auto')
    service = build('translate', 'v2', developerKey='AIzaSyBkhnyE-dN-GpANsLbzDb7csP2F-M2Br_c')
    print('setup done')
    raw_input()

if __name__ == "__main__":
    main()
    #sys.exit(main())
