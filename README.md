# OpnFi Risk

OpnFi Risk is free open source client for Eurex Clearing Enhanced Risk Solution interface AMQP interface (ERS). It provides a UI and REST interface to acces lastest as well as historical data data received over ERS.

## Build
    mvn clean package
    
The shippable artifact will be built in *target/risk-VERSION* directory.

## Managing user database
  Use script *user_manager.sh*. Script accepts one of the following commands:
  - insert
  - delete
  - list

### Insert new user record
      user_manager.sh insert USER PASSWORD
      
### Delete existing user record      
      user_manager.sh delete USER
      
### List all user records      
      user_manager.sh list
