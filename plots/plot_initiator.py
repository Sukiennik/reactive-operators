from plot_executor import *
import pandas as pd
import glob, os

pd.options.display.width = 999999999
pd.options.display.max_columns = 50
pd.options.display.max_colwidth = -1

module_regex = r'\.(combining|conditional|converting|creating|transforming|filtering|math|transforming|utility)\.'
operator_regex = r'.*\.' \
                 r'(?:combining|conditional|converting|creating|transforming|filtering|math|transforming|utility)\.([' \
                 r'a-zA-Z0-9]+)\..*'
solution_regex = r'.*\.(?:combining|conditional|converting|creating|transforming|filtering|math|transforming|utility' \
                 r')\.(?:[a-zA-z0-9]+)\.(?:(Akka|Reactor|RxJava)[a-zA-Z0-9]+).*'
method_regex = r'.*\.(?:combining|conditional|converting|creating|transforming|filtering|math|transforming|utility' \
               r')\.(?:[a-zA-z0-9]+)\.(?:(?:Akka|Reactor|RxJava)[a-zA-Z0-9]+)\.(?:((?:.*)gc-mem\.gcTimeMillis|(?:.*)gc-mem\.total|(?:.+)gc-mem\.used\.after|singleEachOnIo|single|multiEachOnIo|multi)[a-zA-Z0-9]*).*'

filter_out_memory_categories = 'Vm|settled|usedHeap'
perf_methods_categories = ['singleEachOnIo', 'single', 'multiEachOnIo', 'multi']

path = 'results\\**'
all_files = glob.glob(os.path.join(path, "*.csv"))
df_from_each_file = (pd.read_csv(f, delimiter=',') for f in all_files)
concatenated_df = pd.concat(df_from_each_file, ignore_index=True)

results = concatenated_df
results.columns = ['benchmark', 'mode', 'threads', 'samples', 'score', 'error', 'unit', 'times']
results = results[~results['benchmark'].str.contains(filter_out_memory_categories, regex=True)]


byModule = results.benchmark.str.extract(module_regex, expand=False).rename('module')
byOperator = results.benchmark.str.extract(operator_regex, expand=False).rename('operator')
bySolution = results.benchmark.str.extract(solution_regex, expand=False).rename('solution')
byMethod = results.benchmark.str.extract(method_regex, expand=False).rename('method')

performance_results = results.loc[~results['benchmark'].str.contains('gc-mem')]

module_level_aggregator = {
    'benchmark': 'first',
    'score': lambda x: x.tolist(),
    'error': lambda x: x.tolist(),
    'times': lambda x: vectorized_mean(x),
    'unit': 'first',
}

module_level_grouped = performance_results.groupby(by=[byModule, byOperator, bySolution])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(module_level_aggregator)
#full_print(module_level_grouped)

missing_results = calculate_missing_data(performance_results, byOperator.unique())




operator_level_aggregator = {
    'benchmark': 'first',
    'score': lambda x: vectorized_mean(x),
    'error': lambda x: vectorized_mean(x),
    'times': lambda x: vectorized_mean(x),
    'unit': 'first',
}


operator_level_grouped = performance_results.groupby(by=[byModule, byOperator, bySolution])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(operator_level_aggregator)
#full_print(operator_level_grouped)

method_level_aggregator = {
    'benchmark': 'first',
    'score': lambda x: x.tolist(),
    'error': lambda x: x.tolist(),
    'times': lambda x: vectorized_mean(x),
    'unit': 'first',
}

method_level_grouped = results.groupby(by=[byModule, byOperator, bySolution, byMethod])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(method_level_aggregator)
#full_print(method_level_grouped)

# module level, complete solution level + same for memory...

#plot_method_level(method_level_grouped)
#plot_operator_level(operator_level_grouped)
plot_module_level()
plot_complete_level()
