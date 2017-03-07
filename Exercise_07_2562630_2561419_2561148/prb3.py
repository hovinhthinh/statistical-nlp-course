from __future__ import division
from nltk.tokenize import RegexpTokenizer
from pattern.en import parsetree
from os import listdir
from os.path import isfile, join
import re, operator
import numpy as np
from scipy import spatial
stop_word_list = []

def get_stop_word_list(stop_word_file):
	global stop_word_list
	for each in stop_word_file:
		if len(each) > 0:
			stop_word_list.append((each[0:-1]).lower())

def remove_stop_words(data):
	terms_data = {}
	number_terms = 0
	processed = ""
	for word in data.split(' '):
		if word not in stop_word_list:
			number_terms += 1
			processed += " " + word.lower()
			try:
				terms_data[word.lower()] += 1
			except:
				terms_data[word.lower()] = 1
	processed = re.sub('[^A-Za-z ]+', '', processed.lstrip())
	return (processed, terms_data, number_terms)

def tokenize(data):
	processed = ""
	data = data.decode('utf8')
	tokenizer = RegexpTokenizer(r'\w+')
	data = tokenizer.tokenize(data)
	for each in data:
		processed += " " + each
	return (processed.lstrip())

def lemmatize(data):
	processed = ""
	for line in data:
		tree_data = parsetree(line, lemmata = True)
		for each in str(tree_data).split(' '):
			processed += " " + each.split('/')[-1] 
	return (processed.lstrip())

def process_data(data):
	data = lemmatize(data)
	data = tokenize(data)
	#data = ' '.join(data).lower()
	(data, terms_data, num_terms) = remove_stop_words(data)
	return (data, terms_data, num_terms)

def get_vocabulary(data):
	vocab = []
	for word in data.split(' '):
		if (word not in vocab and len(word) > 1):	
			vocab.append(word)
	return (vocab)

def construct_tfidf_doc(top_words, freq_data, doc_len_data, idf):
	top_scoring = {}
	for sub, value in freq_data.iteritems():
		top_scoring[sub] = {}
		for term, freq in value.iteritems():
			if term in top_words:
				top_scoring[sub][term] = (freq/doc_len_data[sub])*idf[term]
	return (top_scoring)

def construct_tfidf(vocab, freq_data, doc_len_data):
	mega_doc_len = 0
	for key, value in doc_len_data.iteritems():
		mega_doc_len += value
	mega_vector = {}
	idf = {}
	for term in vocab:
		freq_count = 0
		num_doc_with_term = 3
		for sub, value in freq_data.iteritems():
			try:
				freq_count += freq_data[sub][term]
			except:
				num_doc_with_term -= 1
		if num_doc_with_term == 0:
			num_doc_with_term = 1
		score = (freq_count/mega_doc_len)*np.log(3/num_doc_with_term)
		mega_vector[term] = score
		idf[term] = np.log(3/num_doc_with_term)
	mega_vector = sorted(mega_vector.items(), key = operator.itemgetter(1), reverse = True)
	top_words = []
	for i in range(0, 400):
		top_words.append(mega_vector[i][0])
	top_scoring = construct_tfidf_doc(top_words, freq_data, doc_len_data, idf)	
	return (top_scoring, top_words, idf)			

def naive_bayes(top_scoring, test_data, vocab_size, doc_len_data):
	max_prob = -9999999999999
	assigned_class = 'physics'
	for sub, value in top_scoring.iteritems():
		prob_score = 0
		for term in test_data.split(' '):
			try:
				prob_score += np.log(value[term])
			except:
				prob_score += np.log((0.000000000000001))
		print ("log of prob. for class: %s is %f" %(sub[0:-4], prob_score))
		if prob_score > max_prob:
			max_prob = prob_score
			assigned_class = sub
	return (assigned_class, max_prob)

def knn(top_scoring, top_words, terms_data, num_terms, idf):
	min_dist = 99999999999999
	assigned_class = "Physics"
	test_vec = [0.0]*len(top_words)
	
	for i in range(0, len(top_words)):
		try:
			test_vec[i] = (terms_data[top_words[i]]/num_terms)*idf[top_words[i]]
		except:
			test_vec[i] = 0.0
	
	for sub, value in top_scoring.iteritems():
		class_vec = [0]*len(top_words)
		for i in range(0, len(top_words)):
			try:
				class_vec[i] = value[top_words[i]]
			except:
				class_vec[i] = 0.0
		dist = spatial.distance.euclidean(class_vec, test_vec)
		print sub, dist
		if dist < min_dist:
			min_dist = dist
			assigned_class = sub[0:-4]
	return assigned_class

if __name__ == "__main__":
	freq_data = {}
	doc_len_data = {}
	training_data_path = 'Materials/train'
	training_files = [f for f in listdir(training_data_path)]
	get_stop_word_list(open('Materials/stopwords.txt', 'r'))
	mega_data = ""
	for file in training_files:
		freq_data[file] = {}
		train_data = open(training_data_path+'/'+file, 'r')
		(train_data, terms_data, num_terms) = process_data(train_data)
		mega_data += " " + train_data
		freq_data[file] = terms_data
		doc_len_data[file] = num_terms
	vocabulary = get_vocabulary(mega_data.lstrip())
	(top_scoring, top_words, idf) = construct_tfidf(vocabulary, freq_data, doc_len_data)
	test_file = open('Materials/test/test_b.txt', 'r')
	(test_data, terms_data, num_terms) = process_data(test_file)
	(assigned_class, prob) = naive_bayes(top_scoring, test_data, len(vocabulary), doc_len_data)
	print ("Assigned class by NB is: %s" %assigned_class[0:-4])
	assigned_class = knn(top_scoring, top_words, terms_data, num_terms, idf)
	print ("Assigned class by knn is: %s" %(assigned_class))
