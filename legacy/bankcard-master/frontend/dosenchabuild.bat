REM set path=C:/Users/xinweiw/bin/Sencha/Architect/Cmd/7.0.0.40;%path%
set path=C:\Users\xinweiw\bin\Sencha\Cmd\7.5.0.5;%path%

REM set the path of the local app server install
set local_app_server=C:\apache-tomcat-9.0.56

REM set the path of the local server project (cloned from gitlab server project repo)
set local_server_proj=C:\MML\git\Bankcard\backend

REM set the path of the local client project (cloned from gitlab client project repo)
set local_client_proj=%local_app_server%\webapps\empbcweb\frontend

set source_testing_build=%local_client_proj%\build\testing\bcp
set source_prod_build=%local_client_proj%\build\production\bcp

set local_prod_build=%local_app_server%\webapps\bcpprod

set dest_testing_build=%local_server_proj%\src\main\webapp\app
set dest_prod_build=%local_server_proj%\src\main\webapp\app
set exclude_file=%local_client_proj%\exclude.txt


sencha app build production
xcopy "%source_prod_build%" "%dest_prod_build%" /exclude:%exclude_file% /s /e /y /d /i

xcopy "%source_prod_build%" "%local_prod_build%" /exclude:%exclude_file% /s /e /y /d /i


REM xcopy "C:\apache-tomcat-9.0.56\webapps\bcpwebnosa\build\production\bcp" "C:\Users\xinweiw\Documents\work\git\restful_bankcard\src\main\webapp\app" /exclude:C:\apache-tomcat-9.0.56\webapps\bcpwebnosa\exclude.txt /s /e /y /d /i
REM xcopy "C:\apache-tomcat-9.0.56\webapps\bcpwebnosa\build\production\bcp" "C:\apache-tomcat-9.0.56\webapps\bcpprod" /exclude:C:\apache-tomcat-9.0.56\webapps\bcpwebnosa\exclude.txt /s /e /y /d /i