# -*- coding: utf-8 -*-
"""
Created on Thu Jun 16 18:59:26 2016

@author: nurzatrakhman
"""

import glob
from nltk.tokenize import RegexpTokenizer
from pattern.en import parsetree
import re
from decimal import Decimal
import math
import heapq

stop_word_list = []


def get_stop_word_list(stop_word_file):
    global stop_word_list
    for each in stop_word_file:
        if len(each) > 0:
            stop_word_list.append((each[0:-1]).lower())


def remove_stop_words(data):
    processed = ""
    for word in data.split():
        word = word.lower()
        if word not in stop_word_list:
            processed += " " + word

    return processed


def tokenize(data):
    # print data
    processed = ""
    # data = data.decode('utf8')
    # data.join()
    tokenizer = RegexpTokenizer(r'\w+')
    data = tokenizer.tokenize(data)
    for each in data:
        processed += " " + each
    return (processed.lstrip())


def lemmatize(data):
    processed = ""
    for line in data:
        tree_data = parsetree(line, lemmata=True)
        for each in str(tree_data).split(' '):
            processed += " " + each.split('/')[-1]
    return (processed.lstrip())


def process_data(data):
    data = ' '.join(data).lower()

    data = lemmatize([data])
    data = tokenize(data)

    data = remove_stop_words(data)

    # print data
    data = data.split(' ')
    data = filter(None, data)
    return data


def create_vocab(data):
    data = set(data)
    data = list(data)
    return data


# ~~~~~~~~~~~~~~~~~~~~~~~~~MPI~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def compute_probs(data, vocab):
    records = []
    probs = []
    lens = len(data)
    count = {}
    for i in data:
        try:
            count[i] += 1
        except:
            count[i] = 1
    for i in range(len(vocab)):
        records.append(count.get(vocab[i], 0))

    for i in range(len(vocab)):
        res = 0
        res = (Decimal(records[i] + 1)) / (Decimal(lens) + Decimal(len(vocab)))
        probs.append(res)

    return probs


def compute_pmi(p_term, p_class):
    records = []
    for i in range(len(p_term)):
        res = 0
        res = Decimal(math.log(p_class[i], 2)) - Decimal(math.log(p_term[i], 2))
        records.append(res)

    return records


def compute_max_pmi(data1, data2, data3):
    records = []
    for i in range(len(data1)):
        res = max(data1[i], data2[i], data3[i])
        records.append(res)

    return records


def feature_top(data, vocab):
    dictionary = dict(zip(vocab, data))
    # top =  sorted(dictionary, key=dictionary.get, reverse=True)[:10]
    top = heapq.nlargest(10, dictionary, key=dictionary.get)

    return top

# ~~~~~~~~~~~~~~~~~~~~~~~~~MI~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def compute_mi(p_class1, p_class2, p_class3):
    records = []

    for i in range(len(p_class1)):
        res = Decimal(p_class1[i] + p_class2[i] + p_class3[i]) / 3
        records.append(res)

    return records


def classify_test(data, p_class, features):
    res = Decimal(0)
    for i in range(len(data)):
        if i in features:
            res = res + Decimal(math.log(p_class[i]))
        else:
            res = res + Decimal(math.log(0.00000000000000000000000000001))

    return res


def get_id(top, vocab):
    ids = []
    for i in range(len(vocab)):
        if vocab[i] in top:
            ids.append(i)

    return ids


def label(res1, res2, res3):
    if (res1 >= res2 and res1 >= res3):
        return "Biology"
    elif (res2 >= res1 and res2 >= res3):
        return "Chemistry"
    else:
        return "Physics"

        # ~~~~~~~~~~~~~~~~~~~~~~~~~MAIN~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


if __name__ == "__main__":
    get_stop_word_list(open('./Materials/stopwords.txt', 'r'))

    training_data1 = open('./Materials/train/Physics.txt', 'r')
    training_data1 = process_data(training_data1)

    training_data2 = open('./Materials/train/Chemistry.txt', 'r')
    training_data2 = process_data(training_data2)

    training_data3 = open('./Materials/train/Biology.txt', 'r')
    training_data3 = process_data(training_data3)

    data = training_data1 + training_data2
    data = data + training_data3

    # construct vocab
    vocab = create_vocab(data)

    # compute probabilities
    prob_term = compute_probs(data, vocab)

    prob_class1 = compute_probs(training_data1, vocab)
    class1_prob = zip(vocab, prob_class1)

    prob_class2 = compute_probs(training_data2, vocab)
    class2_prob = zip(vocab, prob_class2)

    prob_class3 = compute_probs(training_data3, vocab)
    class3_prob = zip(vocab, prob_class3)

    # compute pmi for every term
    pmi_t_class1 = compute_pmi(prob_term, prob_class1)

    pmi_t_class2 = compute_pmi(prob_term, prob_class2)

    pmi_t_class3 = compute_pmi(prob_term, prob_class3)

    # compute pmi(t) To discriminate well for a single category, then we take the maximum:
    pmi_t = compute_max_pmi(pmi_t_class1, pmi_t_class2, pmi_t_class3)

    # select top 10 feature  for pmi(t)
    top1 = feature_top(pmi_t, vocab)

    # compute Mutial Information and select 10 features
    mi_t = compute_mi(pmi_t_class1, pmi_t_class2, pmi_t_class3)

    top2 = feature_top(mi_t, vocab)
    print 'Features for Pointwise MI: ', top1
    print 'Features for MI: ', top2

    inputs = glob.glob('./Materials/test/*')

    print '----------'
    for doc in inputs:
        print 'Classify document:', doc
        test_data = open(doc)
        test_data = process_data(test_data)
        feature_id1 = get_id(top1, vocab)
        feature_id2 = get_id(top2, vocab)

        # using the pmi features
        res1 = classify_test(test_data, prob_class1, feature_id1)
        res2 = classify_test(test_data, prob_class2, feature_id1)
        res3 = classify_test(test_data, prob_class3, feature_id1)

        label1 = label(res1, res2, res3)
        print 'Using PMI:', label1

        # using mi features
        res4 = classify_test(test_data, prob_class1, feature_id2)
        res5 = classify_test(test_data, prob_class2, feature_id2)
        res6 = classify_test(test_data, prob_class3, feature_id2)

        label2 = label(res4, res5, res6)
        print 'Using MI:', label2
        print ''
