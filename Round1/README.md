本次项目的负责人是：张明生
# Round 1 介绍
一.环境搭建：hadoop-2.6.5版本的开发环境
1.三台机器，一个master，两个slave，master的ip为192.168.2.54，slave1的IP为192.168.2.55，slave2的IP为192.168.2.56。通过克隆使所有机器的安装目录一致，网络连接使用桥接模式。
2.安装jdk1.8.0_112，下载安装包，解压缩，然后环境配置
vi /etc/profile
export JAVA_HOME=/home/hadoop/jdk1.8.0_112
export PATH=$PATH:$JAVA_HOME/bin
然后source /etc/profile
3.安装hadoop-2.6.5，下载安装包，解压缩，
配置相关文件，设置虚拟机名字，vi /etc/hostname.
设置映射关系，vi /etc/hosts.
配置hadoop环境:
 $HADOOP_HOME/etc/hadoop/hadoop-env.sh
 $HADOOP_HOME/etc/hadoop/yarn-env.sh
 $HADOOP_HOME/etc/hadoop/core-site.xml
 $HADOOP_HOME/etc/hadoop/hdfs-site.xml
 $HADOOP_HOME/etc/hadoop/mapred-site.xml
 $HADOOP_HOME/etc/hadoop/yarn-site.xml
 $HADOOP_HOME/etc/hadoop/slaves
建立互信关系:
master上生成公私钥，ssh-keygen；复制公钥，cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
slave上ssh-keygen，scp master:~/.ssh/authorized_keys      /home/hadoop/.ssh/
测试连接：ssh slave1
启动hadoop:
先初始化hadoop的文件系统：./hadoop namenode –format
启动：./start-all.sh
输入jps，如果有SecondaryNameNode，Jps，Resourcemanager进程，则正常
输入./hadoop fs –ls /
能正常显示文件系统，则搭建成功。
二.主要工作：
1.第一个完成每天的UV统计：每个独立用户相当于一个Ip，通过统计IP数量来完成对UV的统计，输入数据到map中，正则表达式匹配出IP，还需要去重，将ip放到hash中，遍历输出到文件中，最后计算hash的长度。
2.完成每天访问量Top10的Show统计：输入数据到map中，正则表达式取出IP和show的部分，然后show部分为key,IP为value输出
在reduce部分对value遍历，放到hash数组中，将show部分作为key,hash数组长度做为value，对key排序，输出，在文件中取前10个数据。
完成每天的次日留存统计：工作1中，统计了每天的IP，每天的IP在不同的文件中，相邻两天的IP数据输入到map中，sum为每天IP的总数，把头一天的IP放到list数组中，然后将第二天的数据与list比较，判断list中是否有这个IP，如果有则变量count+1，最后日期为key,count/sum为value输出。

