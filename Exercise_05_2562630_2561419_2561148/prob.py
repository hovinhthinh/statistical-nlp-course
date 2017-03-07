from collections import Counter

input_list = ['all', 'this', 'happened', 'more', 'or', 'less', 'or', 'less']

def find_ngrams(input_list, n):
	return zip(*[input_list[i:] for i in range(n)])
  
def count_ngrams_hierarchical(text, n):
	counts={}
	for j in range(n):
		counts[j+1]=Counter(zip(*[input_list[i:] for i in range(j+1)]))
	return counts 
	
counts=count_ngrams_hierarchical(input_list, 2)
for key, l in counts.items():
	for g,c in l.items():
		print(key,g,c)
