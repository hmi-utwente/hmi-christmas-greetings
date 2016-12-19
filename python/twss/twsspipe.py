import sys
import getopt
from processSentence import *
from svmutil import *
import pickle
import random
import fileinput


def twss(sentence,vocabList,model):
    #print "you said: '"+sentence+"'\n"
    # these should be moved to file
    x  = processSentence(sentence, vocabList)
    #print [x]
    p_label, p_acc, p_val = svm_predict([1], [x], model, '-b 1 -q')
    print p_label, p_acc, p_val
    if p_label[0] == 1:
        return "That's what she said!\n"

def twss_lite(sentence,vocabList,model):
    x = processSentence(sentence, vocabList)
    p_label, p_acc, p_val = svm_predict([1], [x], model, '-b 1 -q')
    return p_label[0]


class Usage(Exception):
    def __init__(self, msg):
        self.msg = msg
        
# based on Guido's post: http://www.artima.com/weblogs/viewpost.jsp?thread=4829
# might be out of date for 2.7 ...
def main(argv=None):
    try:
        input = open('data/vocab.pk')
        vocabList = pickle.load(input)
        input.close()
        model = svm_load_model("data/svm_model.pk")
        for line in fileinput.input():
            print line 
            print twss(line,vocabList,model)
    except Usage, err:
        print >>sys.stderr, err.msg
        print >>sys.stderr, "for help use --help"
        return 2

if __name__ == "__main__":
    sys.exit(main())
