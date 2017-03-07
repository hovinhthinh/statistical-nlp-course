import glob
import math
import string

def remove_punctuation(str):
    for c in list(string.punctuation):
        str = str.replace(c, ' ')
    return str

class category:
    def __init__(self):
        self.termCount = {}
        self.N = 0

    def train(self, content):
        self.N += len(content)

        for term in content:
            if term not in self.termCount:
                self.termCount[term] = 0
            self.termCount[term] += 1

    def prob(self, terms):
        alpha = 0.1
        result = 0
        for term in terms:
            result += math.log((self.termCount.get(term, 0) + alpha) / (self.N + alpha * len(self.termCount)))
        return result


class classifier:
    def __init__(self):
        self.labelCat = {}
        self.labelCount = {}

    def train(self, content, lb):
        if lb not in self.labelCat:
            self.labelCat[lb] = category()
            self.labelCount[lb] = 0

        self.labelCat[lb].train(content)
        self.labelCount[lb] += 1

    def classify(self, content):
        maxP = -1000000000
        maxLabel = 'null'
        for label in self.labelCat:
            pLabel = math.log(float(self.labelCount[label]) / sum(cnt for cnt in self.labelCount.values()))
            pLabel += self.labelCat[label].prob(content)
            if (pLabel > maxP):
                maxP = pLabel
                maxLabel = label

        return maxLabel


austen = glob.glob("./ex-6-materials/austen/*")
twain = glob.glob("./ex-6-materials/twain/*")
test = glob.glob("./ex-6-materials/test/*")
cls = classifier()

for doc in austen:
    content = remove_punctuation(open(doc).read().lower()).split()
    cls.train(content, 'Jane Austen')

for doc in twain:
    content = remove_punctuation(open(doc).read().lower()).split()
    cls.train(content, 'Mark Twain')

for doc in test:
    content = remove_punctuation(open(doc).read().lower()).split()
    print 'Class for', doc, ':', cls.classify(content)
