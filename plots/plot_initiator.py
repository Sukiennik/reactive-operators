from plot_executor import *
import pandas as pd
import glob
import os

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
performance_missing_results = calculate_missing_data(performance_results, byOperator.unique())
performance_results = pd.concat([performance_results, performance_missing_results], ignore_index=True).sort_values(['benchmark', 'times'])

byModulePerf = performance_results.benchmark.str.extract(module_regex, expand=False).rename('module')
byOperatorPerf = performance_results.benchmark.str.extract(operator_regex, expand=False).rename('operator')
bySolutionPerf = performance_results.benchmark.str.extract(solution_regex, expand=False).rename('solution')
byMethodPerf = performance_results.benchmark.str.extract(method_regex, expand=False).rename('method')

gc_mem_total_results = results.loc[results['benchmark'].str.contains('gc-mem.total')]
gc_mem_total_missing_results = calculate_missing_data(gc_mem_total_results, byOperator.unique())
gc_mem_total_results = pd.concat([gc_mem_total_results, gc_mem_total_missing_results], ignore_index=True)

byModuleGcTotal = gc_mem_total_results.benchmark.str.extract(module_regex, expand=False).rename('module')
byOperatorGcTotal = gc_mem_total_results.benchmark.str.extract(operator_regex, expand=False).rename('operator')
bySolutionGcTotal = gc_mem_total_results.benchmark.str.extract(solution_regex, expand=False).rename('solution')
byMethodGcTotal = gc_mem_total_results.benchmark.str.extract(method_regex, expand=False).rename('method')

gc_mem_after_results = results.loc[results['benchmark'].str.contains('gc-mem.used.after')]
gc_mem_after_missing_results = calculate_missing_data(gc_mem_after_results, byOperator.unique())
gc_mem_after_results = pd.concat([gc_mem_after_results, gc_mem_after_missing_results], ignore_index=True)

byModuleGcAfter = gc_mem_after_results.benchmark.str.extract(module_regex, expand=False).rename('module')
byOperatorGcAfter = gc_mem_after_results.benchmark.str.extract(operator_regex, expand=False).rename('operator')
bySolutionGcAfter = gc_mem_after_results.benchmark.str.extract(solution_regex, expand=False).rename('solution')
byMethodGcAfter = gc_mem_after_results.benchmark.str.extract(method_regex, expand=False).rename('method')

gc_mem_time_results = results.loc[results['benchmark'].str.contains('gc-mem.gcTimeMillis')]
gc_mem_time_missing_results = calculate_missing_data(gc_mem_time_results, byOperator.unique())
gc_mem_time_results = pd.concat([gc_mem_time_results, gc_mem_time_missing_results], ignore_index=True)

byModuleGcTime = gc_mem_time_results.benchmark.str.extract(module_regex, expand=False).rename('module')
byOperatorGcTime = gc_mem_time_results.benchmark.str.extract(operator_regex, expand=False).rename('operator')
bySolutionGcTime = gc_mem_time_results.benchmark.str.extract(solution_regex, expand=False).rename('solution')
byMethodGcTime = gc_mem_time_results.benchmark.str.extract(method_regex, expand=False).rename('method')


complete_level_aggregator = {
    'benchmark': 'first',
    'score': lambda x: x.mean(),
    'error': lambda x: x.mean(),
    'times': lambda x: ['N/A'],
    'unit': 'first',
}

complete_level_grouped_perf = performance_results.groupby(by=[bySolutionPerf])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(complete_level_aggregator)
complete_level_grouped_gc_total = gc_mem_total_results.groupby(by=[bySolutionGcTotal])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(complete_level_aggregator)
complete_level_grouped_gc_after = gc_mem_after_results.groupby(by=[bySolutionGcAfter])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(complete_level_aggregator)
complete_level_grouped_gc_time = gc_mem_time_results.groupby(by=[bySolutionGcTime])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(complete_level_aggregator)
# full_print(complete_level_grouped_perf)


module_level_aggregator = {
    'benchmark': 'first',
    'score': lambda x: x.mean(),
    'error': lambda x: x.mean(),
    'times': lambda x: ['N/A'],
    'unit': 'first',
}

module_level_grouped_perf = performance_results.groupby(by=[byModulePerf, bySolutionPerf])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(module_level_aggregator)
module_level_grouped_gc_total = gc_mem_total_results.groupby(by=[byModuleGcTotal, bySolutionGcTotal])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(module_level_aggregator)
module_level_grouped_gc_after = gc_mem_after_results.groupby(by=[byModuleGcAfter, bySolutionGcAfter])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(module_level_aggregator)
module_level_grouped_gc_time = gc_mem_time_results.groupby(by=[byModuleGcTime, bySolutionGcTime])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(module_level_aggregator)
# full_print(module_level_grouped_perf)
# full_print(module_level_grouped_gc_total)
# full_print(module_level_grouped_gc_after)
# full_print(module_level_grouped_gc_time)


operator_level_aggregator = {
    'benchmark': 'first',
    'score': lambda x: vectorized_mean(x),
    'error': lambda x: vectorized_mean(x),
    'times': lambda x: vectorized_mean(x),
    'unit': 'first',
}

operator_level_grouped_perf = performance_results.groupby(by=[byModulePerf, byOperatorPerf, bySolutionPerf])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(operator_level_aggregator)
operator_level_grouped_gc_total = gc_mem_total_results.groupby(by=[byModuleGcTotal, byOperatorGcTotal, bySolutionGcTotal])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(operator_level_aggregator)
operator_level_grouped_gc_after = gc_mem_after_results.groupby(by=[byModuleGcAfter, byOperatorGcAfter, bySolutionGcAfter])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(operator_level_aggregator)
operator_level_grouped_gc_time = gc_mem_time_results.groupby(by=[byModuleGcTime, byOperatorGcTime, bySolutionGcTime])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(operator_level_aggregator)
# full_print(operator_level_grouped_perf)
# full_print(operator_level_grouped_gc_total)
# full_print(operator_level_grouped_gc_after)
# full_print(operator_level_grouped_gc_time)


method_level_aggregator = {
    'benchmark': 'first',
    'score': lambda x: x.tolist(),
    'error': lambda x: x.tolist(),
    'times': lambda x: x.tolist(),
    'unit': 'first',
}

method_level_grouped = results.groupby(by=[byModule, byOperator, bySolution, byMethod])[
    'benchmark', 'score', 'error', 'times', 'unit'].agg(method_level_aggregator)
# full_print(method_level_grouped)


# plot_method_level(method_level_grouped)

# plot_operator_level(operator_level_grouped_perf)
# plot_operator_level(operator_level_grouped_gc_total)
# plot_operator_level(operator_level_grouped_gc_after)
# plot_operator_level(operator_level_grouped_gc_time)

# plot_module_level(module_level_grouped_perf)
# plot_module_level(module_level_grouped_gc_total)
# plot_module_level(module_level_grouped_gc_after)
# plot_module_level(module_level_grouped_gc_after)

# plot_complete_level(complete_level_grouped_perf)
# plot_complete_level(complete_level_grouped_gc_total)
# plot_complete_level(complete_level_grouped_gc_after)
# plot_complete_level(complete_level_grouped_gc_time)

solutions = ['Reactor', 'RxJava', 'Akka Streams']
categories = ['Code documentation', 'Reference guide', 'Ease of use',
              'Accessible syntax', 'Community value', 'Tasks completion']
reactor = [7.4, 7.9, 6.7, 7, 8, 7.7]
rxjava = [6.7, 4, 6, 6.8, 8.9, 7.1]
akka = [5.1, 6.6, 4.8, 5.9, 6.3, 6.5]
assessments = pd.DataFrame(
    {'benchmark': 'code_assessments', 'score': [reactor, rxjava, akka], 'error': [reactor, rxjava, akka],
     'times': [categories, categories, categories],
     'unit': 'Points'})
plot_single(assessments, solutions, None, None, None, 'assessments\\')
print assessments
