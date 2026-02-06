BEGIN
  SYS.DBMS_SCHEDULER.DROP_JOB
    (job_name  => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS');
END;
/

BEGIN
  SYS.DBMS_SCHEDULER.CREATE_JOB
    (
       job_name        => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
      ,start_date      => TO_TIMESTAMP_TZ('2020/06/11 07:00:00.000000 -05:00','yyyy/mm/dd hh24:mi:ss.ff tzr')
      ,repeat_interval => 'FREQ=DAILY'
      ,end_date        => NULL
      ,job_class       => 'DEFAULT_JOB_CLASS'
      ,job_type        => 'PLSQL_BLOCK'
      ,job_action      => 'BEGIN  update_app_settings; commit; END;'
      ,comments        => 'Refresh app settings data based on DB environment'
    );
  SYS.DBMS_SCHEDULER.SET_ATTRIBUTE
    ( name      => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
     ,attribute => 'RESTARTABLE'
     ,value     => FALSE);
  SYS.DBMS_SCHEDULER.SET_ATTRIBUTE
    ( name      => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
     ,attribute => 'LOGGING_LEVEL'
     ,value     => SYS.DBMS_SCHEDULER.LOGGING_OFF);
  SYS.DBMS_SCHEDULER.SET_ATTRIBUTE_NULL
    ( name      => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
     ,attribute => 'MAX_FAILURES');
  SYS.DBMS_SCHEDULER.SET_ATTRIBUTE_NULL
    ( name      => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
     ,attribute => 'MAX_RUNS');
  SYS.DBMS_SCHEDULER.SET_ATTRIBUTE
    ( name      => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
     ,attribute => 'STOP_ON_WINDOW_CLOSE'
     ,value     => FALSE);
  SYS.DBMS_SCHEDULER.SET_ATTRIBUTE
    ( name      => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
     ,attribute => 'JOB_PRIORITY'
     ,value     => 3);
  SYS.DBMS_SCHEDULER.SET_ATTRIBUTE_NULL
    ( name      => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
     ,attribute => 'SCHEDULE_LIMIT');
  SYS.DBMS_SCHEDULER.SET_ATTRIBUTE
    ( name      => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
     ,attribute => 'AUTO_DROP'
     ,value     => FALSE);
  SYS.DBMS_SCHEDULER.SET_ATTRIBUTE
    ( name      => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
     ,attribute => 'RESTART_ON_RECOVERY'
     ,value     => FALSE);
  SYS.DBMS_SCHEDULER.SET_ATTRIBUTE
    ( name      => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
     ,attribute => 'RESTART_ON_FAILURE'
     ,value     => FALSE);
  SYS.DBMS_SCHEDULER.SET_ATTRIBUTE
    ( name      => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS'
     ,attribute => 'STORE_OUTPUT'
     ,value     => TRUE);

  SYS.DBMS_SCHEDULER.ENABLE
    (name                  => 'BCPMS_OWNER.JOB_UPDT_APP_SETTINGS');
END;
/
