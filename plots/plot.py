import matplotlib.pyplot as plt
import pandas as pd

from utils import *

pd.options.display.width = 1000
pd.options.display.max_columns = 50
pd.options.display.max_colwidth = -1

moduleRegex = r'\.(combining|conditional|converting|creating|transforming|filtering|math|transforming|utility)\.'
operatorRegex = r'.*\.' \
                r'(?:combining|conditional|converting|creating|transforming|filtering|math|transforming|utility)\.([' \
                r'a-zA-Z0-9]+)\..*'
solutionRegex = r'.*\.(?:combining|conditional|converting|creating|transforming|filtering|math|transforming|utility' \
                r')\.(?:[a-zA-z0-9]+)\.(?:(Akka|Reactor|RxJava)[a-zA-Z0-9]+).*'
methodRegex = r'.*\.(?:combining|conditional|converting|creating|transforming|filtering|math|transforming|utility' \
              r')\.(?:[a-zA-z0-9]+)\.(?:(?:Akka|Reactor|RxJava)[a-zA-Z0-9]+)\.(?:((?:.*)gc-mem\.gcTimeMillis|(?:.*)gc-mem\.total|(?:.+)gc-mem\.used\.after|singleEachOnIo|single|multiEachOnIo|multi)[a-zA-Z0-9]*).*'

filterOutMemoryResults = 'Vm|settled|usedHeap'

# resultsPath = 'build\\reports\\jmh'
# outputDir = 'plots'
#
# onlyfiles = [f for f in listdir(resultsPath) if isfile(join(resultsPath, f))]
# print onlyfiles

# with open(join(outputDir, 'testFile'), "w") as handle:
#     json.dump(onlyfiles, handle)

# df = pd.read_csv('results\\results_avg.csv', delimiter=',')
df = pd.read_csv('results\\combining\\results.csv', delimiter=',')
df.columns = ['benchmark', 'mode', 'threads', 'samples', 'score', 'error', 'unit', 'times']
df = df[~df['benchmark'].str.contains(filterOutMemoryResults, regex=True)]

byModule = df.benchmark.str.extract(moduleRegex, expand=False).rename('module')
byOperator = df.benchmark.str.extract(operatorRegex, expand=False).rename('operator')
bySolution = df.benchmark.str.extract(solutionRegex, expand=False).rename('solution')
byMethod = df.benchmark.str.extract(methodRegex, expand=False).rename('method')
# print byModule, byOperator, byOperator, byMethod

aggregator = {
    'benchmark': 'first',
    'score': lambda x: x.tolist(),
    'error': lambda x: x.tolist(),
    'times': lambda x: x.tolist(),
    'unit': 'first',
}

multiGrouped = df.groupby(by=[byModule, byOperator, bySolution, byMethod])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(
    aggregator)
# print(multiGrouped)

singlePlot = False

modules = multiGrouped.index.unique(level='module').values
for module in modules:
    moduleGroup = multiGrouped.loc[module]
    operators = moduleGroup.index.unique(level='operator').values

    print moduleGroup

    for operator in operators:
        operatorGroup = moduleGroup.loc[operator]
        methods = operatorGroup.index.unique(level='method').values

        for method in methods:
            flatOperatorGroup = operatorGroup.reset_index()
            methodGroup = flatOperatorGroup.loc[flatOperatorGroup['method'] == method]

            if singlePlot is False:
                #singlePlot = True

                paramsNames = methodGroup[['times']].iloc[0][0]
                solutionsNames = methodGroup['solution'].values
                unit = methodGroup['unit'].values[0]
                if unit == 'bytes':
                    unit = 'MBytes'
                    methodGroup[['score']] = methodGroup[['score']].apply(lambda x: map_array_to_mb(x))
                    methodGroup[['error']] = methodGroup[['error']].apply(lambda x: map_array_to_mb(x))

                results = methodGroup[['score']].unstack().apply(pd.Series)
                errors = methodGroup[['error']].unstack().apply(pd.Series).rename(index={'error': 'score'})

                fig, ax = plt.subplots()
                results.plot.bar(ax=ax, yerr=errors, rot=0, cmap='RdBu', fontsize=8, width=0.5, figsize=(6.5, 5),
                                 capsize=2)
                shouldPlotLog = should_plot_log(results)

                ax.set_xlabel('Solution')
                ax.set_ylabel(unit, rotation=90)
                ax.legend(paramsNames, title="Param: times", loc=1, fontsize='small')
                ax.set_xticklabels(solutionsNames)
                ax.set_title(r"$\bfType$: {}, $\bfGroup$: {}, $\bfOperator$: {}, $\bfMethod$: {}".format(
                    get_benchmark_type(method), module, operator, get_benchmark_method_type(method)), fontsize=8)
                shouldPlotLog and plt.yscale('log')

                name = 'plots\\' + get_directory(module, operator, method)
                save_fig(fig, name)
                plt.yscale('linear')
