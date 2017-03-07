#! /usr/bin/env python3
'''
Created on July 11, 2015

@author: janis, kata
'''

from collections import Counter
from matplotlib import pyplot as plt

def count_ngrams_hierarchical(text, n):
	counts={}
	for j in range(n):
		counts[j+1]=Counter(zip(*[text[i:] for i in range(j+1)]))
	return counts 

def compute_perplexity(jointProbabilityTest,distributionModel):
    from math import log
    base = 2
    loglikelihood = -sum(testNgramCount*log(distributionModel[testNgram],base)
                        for testNgram,testNgramCount in jointProbabilityTest.items())

    perp = base**loglikelihood
    return perp

class discounting_model:
    def __init__(self, d, unigramCounts, bigramCounts):
        self._bigramCounts = bigramCounts
        self._unigramCounts = unigramCounts
        self._d = d
        # trainTokens = set(bigram[-1] for bigram in bigramCounts.keys())
        self._V = len( unigramCounts)

        '''

        TODO: Complete this part to return the R values, as in slide 21 of chapter 5.
        This must be a dict with histories as keys (these are tuples of 1 word); the value of each
        entry should amount to the number of words following that history with non-zero count in the training set.

        '''
        self.R={}
        # begin
        for bigram in bigramCounts.keys():
            if (bigram[0],) not in self.R.keys():
                self.R[(bigram[0],)] = 1
            else:
                self.R[(bigram[0],)] += 1
        # end

    def __getitem__(self, bigram):

        bigramCount = self._bigramCounts.get(bigram, 0)
        history = bigram[0:1]
        historyCount = self._unigramCounts.get(history, 0)
        unigram= bigram[1:]
        unigramCount=self._unigramCounts.get(unigram, 0)

        V, d = self._V, self._d
        N = sum(self._unigramCounts.values())

        if history in self.R.keys():
            nPlus = self.R[history]
        else:
            nPlus = 0

        '''
        TODO: compute the discounted probability.
        
        For your convenience the following values are pre-computed:
            V:            the size of the vocabulary, same as R in the root in slide 22 of chapter 5
            nPlus:        the number of tokens following the current history with non-zero counts.
            N:            the number of tokens in the training set
            d:            discounting parameter
            historyCount: occurrence count of the bigram history (the first bigram token)
            bigramCount:  occurrence count of the bigram itself
            unigramCount: occurence count of the word
            
        '''
        prob=0

        #begin
        alpha = float(d) * V / N;
        if unigramCount > 0:
            pw = (float(unigramCount) - d) / N + alpha / V
        else:
            pw = alpha / V

        if historyCount == 0:
            prob = pw
        else:
            alphah = float(d) * nPlus / historyCount

            if bigramCount > 0:
                prob = (float(bigramCount) - d) / historyCount + alphah * pw
            else:
                prob = alphah * pw
        #end
        return prob

def kfold_crossvalidation(training, d, numFolds):
    '''TO DO: Change this code so that it returns the average perplexity for numFolds different folds
    The code below does it for one specific fold, write a loop that creates all numFolds folds'''
    foldSize=len(training)//numFolds
    perplexity=0
    print 'K-fold cross validation for d =', d
    for i in range(0, numFolds):
        print 'Fold ', (i + 1), '/', numFolds
        validationSet = training[i*foldSize:][:foldSize]
        trainingSet = training[:i*foldSize] + training[(1+i)*foldSize:]

        ''' no need to change things from here ---- '''
        ngramCountsTrain = count_ngrams_hierarchical(trainingSet, 2)
        ngramCountsValidation = count_ngrams_hierarchical(validationSet, 2)
        N=sum(ngramCountsValidation[1].values())-1
        jointDistribution= {bigram : float(bigramCount)/N for bigram,bigramCount in ngramCountsValidation[2].items()}
        mdl=discounting_model(d, ngramCountsTrain[1], ngramCountsTrain[2])
        assert(abs(sum(jointDistribution.values())-1) < 1e-4)
        # Verify that the discounted conditional probability for P(w|'the') sums to one
        assert(abs(sum(mdl[('the',token)] for token in set(trainingSet))-1) < 1e-4)

        '''---- to here'''

        perplexity += compute_perplexity(jointDistribution, mdl )


    return float(perplexity) / numFolds
	
	 
if __name__ == "__main__":

    training= open('text.txt', 'r').read().split()
    numFolds=3
    shouldPlot = True
    if shouldPlot:
        perplexity_of_d = lambda d: kfold_crossvalidation(training, d, numFolds)
        x=tuple(float(x)/10 for x in range(1,11))
        y=tuple(perplexity_of_d(d) for d in x)
        plt.clf()
        plt.xlabel('d parameter')
        plt.ylabel('Perplexity')
        plt.plot(x,y)
    plt.savefig('perplexityC.pdf')
    print 'Done!'

