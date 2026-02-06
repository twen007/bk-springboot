<%@page import="gov.nist.oism.asd.empbc.config.PropertyLoader"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>

        <title>Expense Management Program - OU Bankcard Purchase Request</title>
        
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">


         <%-- Clickjack frame burst --%>
         <style id="antiClickjack">body { display:none !important; }</style>
         <script type="text/javascript">
             if (self === top) {
                 var antiClickjack = document.getElementById("antiClickjack");
                 antiClickjack.parentNode.removeChild(antiClickjack);
             } else {
                 top.location = self.location;
             }
         </script>
                
        <script src="/empbc/JavaScriptServlet"></script>
        
        <style>
            body {
                color: #0B333C;  
                background: #ffffff;
                font-size: 9pt; 
                font-family: "Verdana", Sans-serif;
                margin: 0;
            }

            img
            {  border-style: none;
            }

            #content {
                width: 750px;
                position: absolute;
                height: 40em; 
                top: 40%;
                left: 50%;
                margin-top: -18em;
                margin-left: -375px;
                text-align: center;
            }

            #login-panel {
                -webkit-border-radius:13px;
                -moz-border-radius:13px;
            }

            #error-message {
                color: red;  
                font-weight: bold;
                margin: 1em;
                display: none;
            }

            #login-panel {
                padding: 15px;
                text-align: center;
                border: 1px solid #6699FF;
                width: 500px;
                margin: auto;
                background: #ffffff;
            }
            
            #login-panel form {
                margin: 0;
            }
            
            #login-panel ol {
                list-style: none;
                width: 305px;
                margin: 2em 0 2em 50px;
            }
            
            #login-panel ol li {
                margin-top: 0.4em;
            }
            #login-panel label {
                float: left;
                width: 12em;
                text-align: right;
            }
            
            #login-panel input {
                width: 11em;
            }
            
            #login-panel .required {
                color: red;
            }
            
            #login-panel .submit-button {
                width: 62px;
                height: 22px;
                border: 0;
                background: transparent url("/empbc/button_normal.png");
                margin-bottom: 1em;
            }
            
            #login-panel .submit-button:hover {
                background: transparent url("/empbc/button_hover.png");
            }
            
            #login-panel .submit-button:active {
                background: transparent url("/empbc/button_pressed.png");
            }

            #login-panel a {
                color: blue;
                font-weight: bold;
            }

            #contact {
                font-size: 8pt;
                font-weight: bold;
                margin-top: 1em;
            }  
        </style>

        <script type="text/JavaScript">

            function parseDocumentURL() {
                var args = new Object();

                /* Get argument part (.search is broken on some browers) */
                var url = document.URL;
                var qmPos = url.indexOf("?");

                if (qmPos > -1) {
                    /* Split into the indiviual key=value pairs */
                    var parts = url.substr(qmPos + 1).split("&");
                    for (var i = 0; i < parts.length; i++) {
                        var eqPos = parts[i].indexOf("=");
                        if (eqPos == -1) {
                            args[parts[i]] = null;
                        }
                        else {
                            args[parts[i].substr(0, eqPos)] = decodeURI(parts[i].substr(eqPos + 1));
                        }
                    }
                }

                return args;
            }
            
            function doErrorMsgCheck() {
                var args = parseDocumentURL();
                if (args["err"]) {
                    var ediv = document.getElementById("error-message");
                    ediv.innerHTML = "Authentication Failed";
                    ediv.style.display = "block";
                }
            }

            function initPage() {
                doErrorMsgCheck();
                if (window.location.href.substr(-2) !== "?r") {
                    window.location = window.location.href + "?r";
                }
            }

            function changeFocus(e, next) {
                var enter = window.event ? (e.keyCode == 13) : (e.which == 13);
                if (enter) {
                    next.focus();
                    return false;
                }
                return true;
            }

            function warnCapsLock(e) {
                var ev = e ? e : window.event;
                if (!ev) {
                    return;
                }

                var which = ev.which ? ev.which : (ev.keyCode ? ev.keyCode : -1);
                var shift = ev.shiftKey ? ev.shiftKey : (ev.modifiers ? !!(ev.modifiers & 4) : false);

                if (((which >= 65 && which <= 90) && !shift) || ((which >= 97 && which <= 122) && shift)) {
                    //cannot use alert here because users with caps in their password can never type their password this way
                    //should modify the code to do something like insert a display text below or on the side of the password textbox 
                    //alert("Caps Lock is on");
                }
            }

            function checkForm(form) {
                if (form.j_username.value == "") {
                    alert("Please enter Username.");
                    form.j_username.focus();
                    return false;
                }
                <%
                    if (PropertyLoader.getProperty("use.gendev.password").equals("false")) {
                %>
                if (form.j_password.value == "") {
                    alert("Please enter Password.");
                    form.j_password.focus();
                    return false;
                }
                <%
                    }
                    else {
                %>
                //form.j_password.value = "  abcdefghijkl  mnopqrstuvwxyz{|}~!\"#$%&'()*+,-./0123456789:;>=<?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`  "
                form.j_password.value = <%= "'" + PropertyLoader.getProperty("gendev.password") + "'" %>;
                <%
                    }
                %>
                return true;
            }

        </script>
    </head>

    <body onLoad="initPage()">

        <div id="content">
            
            <p style="font-size: 150%; width: 800px;">
                <font color="blue">**WARNING**WARNING**WARNING**WARNING**WARNING**</font>
            </p>
            
            <p style="font-size: 125%; width: 800px; align-content: center;">
                You are accessing a U.S. Government information system, which includes: 1) this computer, 2) this computer network, 3) all computers connected to this network,
                and 4) all devices and storage media attached to this network or to a computer on this network. You understand and consent to the following:
                you may access this information system for authorized use only; you have no reasonable expectation of privacy regarding any
                communication of data transiting or stored on this information system; at any time and for any lawful Government purpose, the Government may
                monitor, intercept, and search and seize any communication or data transiting or stored on this information system; and any communicationso
                r data transiting or stored on this information system may be disclosed or used for any lawful Government purpose.
            </p>
            
            <p style="font-size: 150%; width: 800px; padding-bottom: 30px;">
                <font color="blue">**WARNING**WARNING**WARNING**WARNING**WARNING**</font>
            </p>

            <p id="error-message">
                Authentication Failed
            </p>

            <div id="login-panel">
                <H4>OU Bankcard Purchase Pre-Approval</h4>
                <b>Please enter your NIST General Realm Username and Password</b>
                <form action="j_security_check" method="post" onSubmit="return checkForm(this)">
                    <ol>
                        <li>
                            <label for="j_username">
                                Username: <span class="required">*</span>
                            </label>
                            <input type="text" id="j_username" name="j_username" size="20" title="Enter your general realm username" onKeyPress="return changeFocus(event, this.form.j_password)"/>
                        </li>
                        <li>
                            <label for="j_password">
                                Password: <span class="required">*</span>
                            </label>
                            <input type="password" id="j_password" name="j_password" size="20" title="Enter your general realm password" onKeyPress="return warnCapsLock(event)"/>
                        </li>
                    </ol>
                    <div>
                        <input type="submit" class="submit-button" value="Login">
                    </div>
                </form>
                <a href="http://passwordportal.nist.gov">
                    Forgot your password?
                </a>
            </div>
            
            <br/>
            
            <div id="contact">
                For assistance please <a href="mailto:mml.systemshelp@nist.gov">email MML Systems Support</a>
            </div>
            
        </div>  
    </body>
</html>

