import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

public class Hive {

	public static class Map extends
			Mapper<LongWritable, Text, Text, NullWritable> {
		private static String pattern = "^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}) [^ ]+ [^ ]+ \\[[^ ]+ [^ ]+\\] \"[^ ]+ ([^ ]+) ";
		private static Pattern r = Pattern.compile(pattern);
		private String sPattern = "^(/\\w+)(/\\d+$|\\?.*)";

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			AddressUtils addressUtils = new AddressUtils();
			String os = null;
			String show = null;
			String refer = null;
			String country = "for";
			String address = "";
			Matcher ma = r.matcher(value.toString());
			if (ma.find()) {
				String[] line = value.toString().split(" ", 13);
				try {
					country = addressUtils.getCountry("ip=" + line[0], "utf-8");
					if (country == "中国") {
						address = addressUtils.getAddresses("ip=" + line[0], "utf-8");
					} else if (country != "中国") {
						address = "foreign";
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (line.length > 12) {
					Pattern rShow = Pattern.compile(sPattern);
					Matcher m = rShow.matcher(line[6]);
					if (m.find()) {
						show = m.group(1);
					} else {
						show = "otherType";
					}
					if (line[12].contains("iPhone")) {
						os = "iOS";
					} else if (line[12].contains("Android")) {
						os = "Android";
					} else {
						os = "otherOS";
					}
					if(line[10].contains("baidu")){
						refer = "www.baidu.com";
					}else if(!line[10].contains("baidu")){
						refer = line[10];
					}
					
					String st = line[3].substring(1, line[3].length());
					String str = line[0] + "\t" +address+ "\t" + st + "\t" + show + "\t"
							+ refer + "\t" + os + "\t" + line[12];
					context.write(new Text(str), NullWritable.get());
				}
				}
		}
	}

	public static class Reduce extends Reducer<Text, NullWritable, Text, NullWritable> {
		public void reduce(Text key, Iterable<NullWritable> values,
				Context context) throws IOException, InterruptedException {
			context.write(key, NullWritable.get());
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "ZMSDataCleaning");
		job.setJarByClass(Hive.class);
		job.setMapperClass(Map.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(NullWritable.class);
		job.setReducerClass(Reduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		job.setNumReduceTasks(1);
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.waitForCompletion(true);
		return;
	}
}
