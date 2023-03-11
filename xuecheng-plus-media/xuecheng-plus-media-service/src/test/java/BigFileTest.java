import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-03-08 20:22
 **/
public class BigFileTest {
    //测试文件分块方法
    @Test
    public void testChunk() throws IOException {
//        1、获取源文件长度
        File sourceFile = new File("E:\\a\\b.mp4");
        long fileSize = sourceFile.length();
//        2、根据设定的分块文件的大小计算出块数
        String chunkPath = "E:\\a\\块\\";

        File chunkFolder = new File(chunkPath);

        long chunkSize = 1024*1024*1;//每块1m大小
        long chunkNum = (long)Math.ceil(fileSize*1.0/chunkSize);
//        3、从源文件读数据依次向每一个块文件写数据。

        //缓冲区大小
        byte[] b = new byte[1024];
        //使用RandomAccessFile访问文件
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile,"r");
        //分块
        for (int i = 0; i < chunkNum; i++) {
            File file = new File(chunkPath + i);
            if (file.exists()){
                file.delete();
            }
            if (file.createNewFile()){
                //向当前块写入数据
                RandomAccessFile raf_rw=new RandomAccessFile(file,"rw");
                int len=-1;
                while ((len=raf_r.read(b))!=-1){
                    raf_rw.write(b,0,len);
                    if (file.length()>=chunkSize){
                        //已经写入当前块设定的最大容量则跳出，对下一个块进行操作
                        break;
                    }
                }
                raf_rw.close();
            }
            }
             raf_r.close();
        }
    @Test
    public void testMerge() throws IOException {
//        文件合并流程：
//        1、找到要合并的文件并按文件合并的先后进行排序。
        //块文件目录
        File chunkFolder = new File("E:\\a\\块\\");
        //原始文件
        File originalFile = new File("E:\\a\\b.mp4");

//        2、创建合并文件
        //合并文件
        File mergeFile = new File("E:\\a\\b01.mp4");
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        //创建新的合并文件
        mergeFile.createNewFile();
//        3、依次从合并的文件中读取数据向合并文件写入数
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
        //指针指向文件顶端
        raf_write.seek(0);
        //缓冲区
        byte[] b = new byte[1024];
        File[] files = chunkFolder.listFiles();
        List<File> fileList = Arrays.asList(files);
        fileList.sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName())-Integer.parseInt(o2.getName());
            }
        });
        fileList.forEach(file -> {
            System.out.println(file.getName());
        });

        for (File file : fileList) {
            RandomAccessFile raf_read = new RandomAccessFile(file, "rw");
            int len=-1;

            while ((len=raf_read.read(b))!=-1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
            raf_write.close();

    }

    }
