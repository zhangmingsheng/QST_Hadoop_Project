import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class DayUV {

	public static class Map extends
			Mapper<LongWritable, Text, Text, NullWritable> {
		private String pattern = "(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)";
		private Pattern r = Pattern.compile(pattern);

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] line = value.toString().split(" ");
			Matcher m = r.matcher(line[0]);
			if (m.find()) {
				context.write(new Text(line[0]), NullWritable.get());
			}
		}
	}

	public static class Reduce extends
			Reducer<Text, NullWritable, Text, LongWritable> {
		long count = 0;
		String date = null;

		public void setup(Context context) throws IOException {
			Configuration conf = context.getConfiguration();
			date = conf.get("Date");
		}

		public void reduce(Text key, Iterable<NullWritable> values,
				Context context) throws IOException, InterruptedException {
			count++;
		}

		public void cleanup(
				Reducer<Text, NullWritable, Text, LongWritable>.Context context)
				throws IOException, InterruptedException {
			super.cleanup(context);
			context.write(new Text("UV " + date + " :"),
					new LongWritable(count));
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("Date", args[2]);
		Job job = Job.getInstance(conf, "ZMSdayUV");
		job.setJarByClass(DayUV.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setMapOutputValueClass(NullWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);
		job.setNumReduceTasks(1);
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.waitForCompletion(true);
		return;
	}
}