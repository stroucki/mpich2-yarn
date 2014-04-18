package com.taobao.yarn.mpi.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.yarn.mpi.util.LocalFileUtils;

public class ContainerMerge  implements Callable<Boolean>{

  private static final Log LOG = LogFactory.getLog(ContainerMerge.class);

  public static final int BUFSIZE = 1024 * 8;

  private final List<String> files;

  private final String outFile;

  public ContainerMerge(List<String> files, String outFile) {
    this.files = files;
    this.outFile = outFile;
  }

  /**
   * merge the files which are downloaded by the container
   */
  //@Override
  public Boolean call() {
    Boolean success = true;
    FileChannel outChannel = null;
    try {
      File fexists = new File(outFile);
      if (fexists.exists()) {
        fexists.delete();
      }

      LocalFileUtils.mkParentDir(outFile);

      outChannel = new FileOutputStream(outFile).getChannel();

      for (String f : files) {
        FileChannel fc = new FileInputStream(f).getChannel();
        ByteBuffer bb = ByteBuffer.allocate(BUFSIZE);
        while (fc.read(bb) != -1) {
          bb.flip();
          outChannel.write(bb);
          bb.clear();
        }
        fc.close();
      }
    } catch (IOException ioe) {
      success = false;
      LOG.error("error while closing the outFle channel:", ioe);
    } finally {
      try {
        if (outChannel != null) {
          outChannel.close();
        }
      } catch (IOException io) {
        success = false;
        io.printStackTrace();
        LOG.error("error while closing the outFle channel:", io);
      }
    }
    return success;
  }
}
