package zhj.notetaking.listener;


/**
 * task listener for Asyntask,to handle task results in activities,etc.
 */
public interface TaskListener {

    /**
     * when task has fetched data
     * @return  is the data has no error message
     */
     boolean onTaskSucceed();

    /**
     * when task got no data, caused by reasons like poor network
     */
     void onTaskFail();

     void asynTaskBeforeSend();
    void asynTaskComplete();
}
