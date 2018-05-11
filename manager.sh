# Simple apache maven&ant packet manager.
# Script was written by Tymur Kvaratskheliia. Email: tymur_kvaratskheliia@epam.com
set -xe

# Pre-installations

if [[ `which java` = *"no java in"* ]]
then
  echo "Installing java 1.8.0..."
  yum install -y java-1.8.0-openjdk-devel
fi

# install wget 
if [[ `yum list installed | grep "wget"` == '' ]]
then
  echo "Installing wget..."
  yum install -y wget
fi

# Define variables
ant_maven=""
location="/opt/mavant"

# Install function
function install {

  ant_maven="${1}"

  # Checking second argument
  if [[ $1 == '' ]]
  then
    echo -e "Please enter what do you want install(Maven or Ant)
Example ./manager install Maven"
    exit 0
  elif [[ $1 != "maven" ]] && [[ $1 != "ant" ]] && [[ $1 != "Maven" ]] && [[ $1 != "Ant" ]]
    then
      echo "Sorry, but this sript can install only Maven or Ant package"
      exit 0
  fi
 
  # Main part
  global_version=${2:0:1}
  echo $gloal_version
  echo "Start $ant_maven instalation..."

  # Create diractory if doesn't exist
  if [ -d $location ]
  then
    cd $location
  else
    mkdir $location
  fi

  if [[ $ant_maven == "maven" ]] || [[ $ant_maven == "Maven" ]]
  then
    
    ant_maven="maven"
    # Check if exists maven
    if [[ `ls $location | grep "apache-$ant_maven-$2"` != '' ]]
    then
      echo "You already have this $ant_maven version"
      exit 0
    fi

    # download maven from official source
    wget "http://www-us.apache.org/dist/maven/maven-$global_version/$2/binaries/apache-maven-$2-bin.tar.gz"
    # unzip maven
    tar -xzf apache-$ant_maven-$2-bin.tar.gz
    # rm maven tar.gz
    rm -f apache-$ant_maven-$2-bin.tar.gz
    # set maven home and add in path
    export M2_HOME=$location/maven
    export PATH=${M2_HOME}/bin:$PATH
    # Set alternatives
    alternatives --insatall $location/$ant_maven $ant_maven $location/apache-$ant_maven-$2/ 1

  else
    
    ant_maven="ant"  
    # download ant
    wget "http://archive.apache.org/dist/ant/binaries/apache-ant-$2-bin.tar.gz"
    # unzip ant      
    tar -xzf apache-$ant_maven-$2-bin.tar.gz
    # rm ant tar.gz
    rm -f apache-$ant_maven-$2-bin.tar.gz
    # set home
    export ANT_HOME=$location/ant
    export PATH=${ANT_HOME}/bin:${PATH}
    # set alternative
    alternatives --install $location/$ant_maven $ant_maven $location/apache-$ant_maven-$2/ 1

  fi
}

# Remove function
function remove {

  ant_maven="${1}"
  
  # Checking second argument
  if [[ $1 == '' ]]
  then
    echo -e "Please, enter what do you want remove(Maven or Ant)
Example ./manager remove Ant"
    exit 0
  elif [[ $1 != "maven" ]] && [[ $1 != "ant" ]] && [[ $1 != "Maven" ]] && [[ $1 != "Ant" ]]
    then
      echo "Sorry, but this script can manage only Maven or Ant packages"
      exit 0
  fi
  
  if [[ $2 != '' ]]
  then
    ant_maven="$ant_maven $2"
  fi
  # Main part
  echo "Start removing $ant_maven-$2"
  rm -rf $location/apche-$ant_maven-$2
  echo "$ant_maven-$2 successfully removed"

}

# Use function
function use {
 
  ant_maven="$1"

  # execute install function with use's parameters
  if [[ `ls $location | grep "apache-$ant_maven-$2"` == '' ]]
  then
    echo "You don't have $2 version of $ant_maven. Please wait for installation" 
    install "$1" "$2" 
  fi
  # Setting alternatives 
  alternatives --install $location/maven maven $location/apache-$ant_maven-$2/ 1
  #alternatives --config maven
  
}

# List function
function list {
  
  if [[ $1 == "Ant" ]] || [[ $1 == "ant" ]]
  then
    # Get list of available ant versions
    list_ant=`curl -X GET https://archive.apache.org/dist/ant/binaries/ | grep -Po "apache-ant-.*(?=-bin.tar.gz\")"`
    cd $location
    echo $list_ant | xargs ls
  elif [[ $1 == "Maven" ]] || [[ $1 == "maven" ]]
  then
    # Get list fo available maven vesions
    list_maven=`curl -X GET https://www-eu.apache.org/dist/maven/binaries/ | grep -Po "apache-maven.*(?=-bin.tar.gz\")"`
    echo $list_maven
  else
    echo "Sory, but this script works only with Ant or Maven"
    exit 0
  fi

}

# Choosing arguments
case "$1" in
  "install")
    echo "You choosed installation"
    install "$2" "$3"                # starting install function
    ;;
  "remove")
    echo "You choosed remove"
    remove "$2" "$3"                 # starting remove function
    ;;
  "use")
    echo "You choosed use mode"
    use "$2" "$3"                    # starting use function
    ;;
  "list")
    echo "You choosed list"
    list "$2"                        # starting list function
    ;;
  "help")
    echo -e "This script easily can download and install Apache Maven or Ant packages.\n
    There are list of arguments you can use:\n
    	install - install specify Maven or Ant version
    	remove - remove chosed Maven or Ant version
        use - you can chose what version do you want to use now ( only CentOS 7+ )
    	help - user manual\n
    Thank you for using script.\n"
    ;;
  *)
    echo "\"$1\" no such argument. User \"help\" to see list of available arguments"
esac

