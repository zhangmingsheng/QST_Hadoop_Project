import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TheNextDayLeft {
	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
		private static Locale locale = Locale.US;
		private static SimpleDateFormat sdf = new SimpleDateFormat(
				"dd/MMM/yyyy", locale);
		private String pattern = "(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)";

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			Date date = null;
			Pattern r = Pattern.compile(pattern);
			String[] line = value.toString().split(" ");
			String[] str = line[3].substring(1, line[3].length()).split(":");
			Matcher m = r.matcher(line[0]);
			if (m.find()) {
				try {
					date = sdf.parse(str[0]);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				String sdate = (new SimpleDateFormat("yyyy-MM-dd"))
						.format(date);
				context.write(new Text(line[0]), new Text(sdate));
			}
		}
	}

	public static class Combine extends Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			HashSet<String> list = new HashSet<String>();
			for (Text t : values) {
				list.add(t.toString());
			}
			for (String s : list) {
				context.write(key, new Text(s));
			}
		}
	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		long count = 0;
		long ipCount = 0;

		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException {
			HashSet<String> list = new HashSet<String>();
			for (Text t : values) {
				list.add(t.toString());
			}
			if (list.size() == 2) {
				ipCount++;
			}
			count++;
		}

		public void cleanup(Context context) throws IOException,
				InterruptedException {
			float left = (float)ipCount/count;
			context.write(new Text("TheNextDayLeft:"), new Text(String.valueOf(left)));
		}
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);
		job.setJarByClass(TheNextDayLeft.class);
		
		job.setMapperClass(Map.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setCombinerClass(Combine.class);

		job.setReducerClass(Reduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setNumReduceTasks(1);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));
		job.waitForCompletion(true);
		return;
	}
}
