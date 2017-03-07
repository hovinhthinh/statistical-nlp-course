#! /usr/bin/env python3
'''
Created on July 11, 2015

@author: janis, kata
'''

from collections import Counter
from matplotlib import pyplot as plt


def count_ngrams_hierarchical(text, n):
    counts = {}
    for j in range(n):
        counts[j + 1] = Counter(zip(*[text[i:] for i in range(j + 1)]))
    return counts


def compute_perplexity(jointProbabilityTest, distributionModel):
    from math import log
    base = 2
    loglikelihood = -sum(jointProbabilityTest[testNgram] * log(distributionModel[testNgram], base)
                         for testNgram in jointProbabilityTest)

    perp = base ** loglikelihood
    return perp


class discounting_model:
    def prune(self, epsilon):
        newUnigramCounts = Counter()
        totalCount = 0
        for key, count in self._unigramCounts.iteritems():
            totalCount += count
        for key, count in self._unigramCounts.iteritems():
            prob = float(count) / totalCount
            if prob >= epsilon:
                newUnigramCounts[key] = count

        newBigramCounts = Counter()
        totalCount = 0
        for key, count in self._bigramCounts.iteritems():
            totalCount += count
        for key, count in self._bigramCounts.iteritems():
            prob = float(count) / totalCount
            if prob >= epsilon:
                newBigramCounts[key] = count
        return discounting_model(self._d, newUnigramCounts, newBigramCounts)

    def __init__(self, d, unigramCounts, bigramCounts):
        self._bigramCounts = bigramCounts
        self._unigramCounts = unigramCounts
        self._d = d
        self._N = sum(self._unigramCounts.values())
        # trainTokens = set(bigram[-1] for bigram in bigramCounts.keys())
        self._V = len(unigramCounts)

        self.R = {}
        # begin
        for bigram in bigramCounts.keys():
            if (bigram[0],) not in self.R:
                self.R[(bigram[0],)] = 1
            else:
                self.R[(bigram[0],)] += 1
        # end

    def __getitem__(self, bigram):

        bigramCount = self._bigramCounts.get(bigram, 0)
        history = bigram[0:1]
        historyCount = self._unigramCounts.get(history, 0)
        unigram = bigram[1:]
        unigramCount = self._unigramCounts.get(unigram, 0)

        V, d = self._V, self._d
        N = self._N

        if history in self.R:
            nPlus = self.R[history]
        else:
            nPlus = 0

        prob = 0

        # begin
        alpha = float(d) * V / N;
        if unigramCount > 0:
            pw = (float(unigramCount) - d) / N + alpha / V
        else:
            pw = alpha / V

        if historyCount == 0 or nPlus == 0:
            prob = pw
        else:
            alphah = float(d) * nPlus / historyCount
            if bigramCount > 0:
                prob = (float(bigramCount) - d) / historyCount + alphah * pw
            else:
                prob = alphah * pw
        # end
        return prob


def perplexity(training, test, d):
    ngramCountsTrain = count_ngrams_hierarchical(training, 2)
    ngramCountsValidation = count_ngrams_hierarchical(test, 2)
    N = sum(ngramCountsValidation[1].values()) - 1
    jointDistribution = {bigram: float(bigramCount) / N for bigram, bigramCount in ngramCountsValidation[2].items()}

    mdl = discounting_model(d, ngramCountsTrain[1], ngramCountsTrain[2])

    assert (abs(sum(jointDistribution.values()) - 1) < 1e-4)
    # Verify that the discounted conditional probability for P(w|'the') sums to one
    assert (abs(sum(mdl[('the', token)] for token in set(training)) - 1) < 1e-4)

    return compute_perplexity(jointDistribution, mdl)


def perplexity_pruning(training, test, d, epsilon):
    ngramCountsTrain = count_ngrams_hierarchical(training, 2)
    ngramCountsValidation = count_ngrams_hierarchical(test, 2)
    N = sum(ngramCountsValidation[1].values()) - 1
    jointDistribution = {bigram: float(bigramCount) / N for bigram, bigramCount in ngramCountsValidation[2].items()}
    mdl = discounting_model(d, ngramCountsTrain[1], ngramCountsTrain[2]).prune(epsilon)

    # assert (abs(sum(jointDistribution.values()) - 1) < 1e-4)
    # Verify that the discounted conditional probability for P(w|'the') sums to one
    # assert (abs(sum(mdl[('the', token)] for token in set(training)) - 1) < 1e-4)

    return compute_perplexity(jointDistribution, mdl)


if __name__ == "__main__":

    training = open('./ex-6-materials/twain/pg119.txt').read().split()
    test = open('./ex-6-materials/twain/pg3176.txt').read().split()

    d = 0.9

    print 'The perplexity for the original absolute discounting model: ', perplexity(training, test, d)
    print
    for n in range(3, 7):
        eps = float(0.1) ** n
        res = perplexity_pruning(training, test, d, eps)
        print 'The perplexity for the pruned model with EPSILON =', eps, ':', res
