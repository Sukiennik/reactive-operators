import pandas as pd

pd.options.display.width = 1000
pd.options.display.max_columns = 50

moduleRegex = r'\.(combining|conditional|converting|creating|transforming|filtering|math|transforming|utility)\.'
operatorRegex = r'.*\.' \
                r'(?:combining|conditional|converting|creating|transforming|filtering|math|transforming|utility)\.([' \
                r'a-zA-Z0-9]+)\..*'
solutionRegex = r'.*\.(?:combining|conditional|converting|creating|transforming|filtering|math|transforming|utility' \
                r')\.(?:[a-zA-z0-9]+)\.(?:(Akka|Reactor|RxJava)[a-zA-Z0-9]+).*'
methodRegex = r'.*\.(?:combining|conditional|converting|creating|transforming|filtering|math|transforming|utility' \
              r')\.(?:[a-zA-z0-9]+)\.(?:(?:Akka|Reactor|RxJava)[a-zA-Z0-9]+)\.(?:(' \
              r'singleEachOnIo|single|multiEachOnIo|multi)[a-zA-Z0-9]*).* '



# resultsPath = 'build\\reports\\jmh'
# outputDir = 'plots'
#
# onlyfiles = [f for f in listdir(resultsPath) if isfile(join(resultsPath, f))]
# print onlyfiles

# with open(join(outputDir, 'testFile'), "w") as handle:
#     json.dump(onlyfiles, handle)

df = pd.read_csv('results\\results_avg.csv', delimiter=',')
df.columns = ['benchmark', 'mode', 'threads', 'samples', 'score', 'error', 'unit', 'times']
print(df)
df.info()

# function to map to group -> operator -> solution
grouped = df.groupby('benchmark')['score', 'error', 'times'].agg(lambda x: x.tolist())
A = df.values
print (A[:, 0])
print('---------------------------------------------------------')

byModule = df.benchmark.str.extract(moduleRegex, expand=False).rename('module')
byOperator = df.benchmark.str.extract(operatorRegex, expand=False).rename('operator')
bySolution = df.benchmark.str.extract(solutionRegex, expand=False).rename('solution')
byMethod = df.benchmark.str.extract(methodRegex, expand=False).rename('method')
aggregator = {
    'benchmark': lambda x: x,
    'score': lambda x: x.tolist(),
    'error': lambda x: x.tolist(),
    'times': lambda x: x.tolist(),
}

moduleGrouped = df.groupby(by=[byModule, byOperator, bySolution, byMethod])['benchmark', 'score', 'error', 'times'] \
    .agg(aggregator)

print(byModule)
print(byOperator)
print(bySolution)
print(byMethod)
print('---------------------------------------------------------')
print(moduleGrouped['benchmark'])
for i in moduleGrouped['benchmark']:
    print i

# df.groupby()


print('---------------------------------------------------------')
print (moduleGrouped.ix[0, 'score'])
print (moduleGrouped['score'][1])
