package ygor

import de.hbznrw.ygor.processing.SendTitlesThreadGokb
import de.hbznrw.ygor.processing.UploadThreadGokb

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJob{

  Enrichment.FileType fileType
  String uuid
  Status status
  def uploadThread
  int total

  UploadJob(Enrichment.FileType fileType, UploadThreadGokb uploadThread){
    this.fileType = fileType
    uuid = UUID.randomUUID().toString()
    status = Status.PREPARATION
    this.uploadThread = uploadThread
    total = uploadThread.total
  }


  void start(){
    status = Status.STARTED
    if (fileType in [Enrichment.FileType.TITLES, Enrichment.FileType.PACKAGE]){
      uploadThread.start()
    }
    // else there is nothing to do
  }


  @SuppressWarnings("JpaAttributeMemberSignatureInspection")
  int getCount(){
    if (fileType.equals(Enrichment.FileType.TITLES)){
      return ((SendTitlesThreadGokb) uploadThread).getCount();
    }
  }


  @SuppressWarnings("JpaAttributeMemberSignatureInspection")
  def getSortedJobInfo(){
    Map<String, String> jobInfo = [:]
    refreshStatus()
    jobInfo.put("status", status)
    jobInfo.put("jobId", uuid)
    jobInfo = uploadThread.getThreadInfo(jobInfo)
    return uploadThread.getResponseSorted(jobInfo)
  }


  void refreshStatus(){
    if (status == Status.STARTED){
      if (uploadThread.getCount() >= uploadThread.total){
        status = Status.FINISHED_UNDEFINED
      }
    }
  }


  enum Status{
    PREPARATION,
    STARTED,
    FINISHED_UNDEFINED,
    SUCCESS,
    ERROR
  }
}