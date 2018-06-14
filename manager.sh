#!/bin/bash
# Simple apache maven&ant packet manager.
# Script was written by Tymur Kvaratskheliia. Email: tymur_kvaratskheliia@epam.com


# Pre-installations
if [[ $(command -v java) = *"no java in"* ]]
then
  echo "Installing java 1.8.0..."
  yum install -y java-1.8.0-openjdk-devel
fi

# install wget
if [[ $(yum list installed | grep "wget") == '' ]]
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
  echo "$global_version"
  echo "Start $ant_maven instalation..."

  # Create diractory if doesn't exist
  if [ -d $location ]
  then
    cd $location || return
  else
    mkdir $location
  fi

  if [[ $ant_maven == "maven" ]] || [[ $ant_maven == "Maven" ]]
  then

    ant_maven="maven"
    # Check if exists maven
    if [[ $(ls $location | grep "apache-$ant_maven-$2") != '' ]]
    then
      echo "You already have this $ant_maven version"
      exit 0
    fi


    # check http response code
    resp=$(wget --spider "http://www-us.apache.org/dist/maven/binaries/apache-maven-$2-bin.tar.gz" 2>&1 | grep ".*HTTP.*")
    if [[ $resp =~ .*404.* ]]
    then
      echo "$ant_maven $2 version not found. Use \"list $ant_maven\" argument to see available versions from official apache storage"
      exit 0
    fi
    # download maven from official source
    wget "http://www-us.apache.org/dist/maven/binaries/apache-maven-$2-bin.tar.gz"
    # unzip maven
    tar -xzf apache-$ant_maven-"$2"-bin.tar.gz
    # rm maven tar.gz
    rm -f apache-$ant_maven-"$2"-bin.tar.gz
    # set maven home and add in path
    echo "M2_HOME=$location/$ant_maven
export M2_HOME
PATH=\$M2_HOME/bin:\$PATH
export PATH" >> /home/vagrant/.bashrc
    # Set alternatives
    alternatives --install $location/$ant_maven $ant_maven $location/apache-$ant_maven-"$2"/ 1

  else

    ant_maven="ant"
    # check http response
    resp=$(wget --spider "http://archive.apache.org/dist/ant/binaries/apache-ant-$2-bin.tar.gz" 2>&1 | grep ".*HTTP.*")
    if [[ $resp =~ .*404.* ]]
    then
      echo "$ant_maven $2 version not found. Use \"list $ant_maven\" argument to see available versions from official apache storage"
      exit 0
    fi
    # download ant
    wget "http://archive.apache.org/dist/ant/binaries/apache-ant-$2-bin.tar.gz"
    # unzip ant
    tar -xzf apache-$ant_maven-"$2"-bin.tar.gz
    # rm ant tar.gz
    rm -f apache-$ant_maven-"$2"-bin.tar.gz
    # set home
    echo "ANT_HOME=$location/$ant_maven
export ANT_HOME
PATH=\$ANT_HOME/bin:\${PATH}
export PATH" >> /home/vagrant/.bashrc
    # set alternative
    alternatives --install $location/$ant_maven $ant_maven $location/apache-$ant_maven-"$2"/ 1

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

  # Main part
  echo "Start removing $ant_maven-$2"
  rm -rf $location/apache-"$ant_maven"-"$2"
  alternatives --remove "$ant_maven" $location/apache-"$ant_maven"-"$2"/
  echo "$ant_maven-$2 successfully removed"

}

# Use function
function use {

  ant_maven="$1"

  if [[ $ant_maven == "Maven" ]] || [[ $ant_maven == "maven" ]]
  then
    ant_maven="maven"
  elif [[ $ant_maven == "Ant" ]] || [[ $ant_maven == "Ant" ]]
  then
    ant_maven="ant"
  else
    echo "Sorry, but this script can manage only Maven and Ant packages."
  fi

  # execute install function with use's parameters
  if [[ $(ls $location | grep "apache-$ant_maven-$2") == '' ]]
  then
    echo "You don't have $2 version of $ant_maven. Please wait for installation"
    install "$1" "$2"
  fi
  # Setting alternatives
  alternatives --install $location/$ant_maven $ant_maven $location/apache-$ant_maven-"$2"/ 1
  alt_number=$(echo "" | alternatives --config maven | grep -Po "(?<=.)[0-9](?=.*-$2/)")
  echo "$alt_number" | alternatives --config $ant_maven
  #alternatives --config maven
  echo "$ant_maven-$2 was set as main. Yor cant check current version using $ant_maven -version command. "

}

# List function
function list {

  if [[ $1 == "Ant" ]] || [[ $1 == "ant" ]]
  then
    ant_maven="ant"
    # Get installed ant versions
    installed=$(ls $location | grep -Po "(?<=.-$ant_maven-).*")
    echo "$installed"
    # Get list of available ant versions
    list_ant=$(curl -s GET https://archive.apache.org/dist/ant/binaries/ | grep -Po "apache-ant-.*(?=-bin.tar.gz\")")
    echo "$list_ant" | sed "s/ /\\n/g" | sed "/.*-$installed/s/$/ /"
  elif [[ $1 == "Maven" ]] || [[ $1 == "maven" ]]
  then
    # Get installed maven versioins
    installed=""
    # Get list fo available maven vesions
    list_maven=$(curl -s GET https://www-us.apache.org/dist/maven/binaries/ | grep -Po "apache-maven.*(?=-bin.tar.gz\")")
    echo "$list_maven" | sed "s/ /\\n/g"
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
    echo "List of available $2 packages in official storage https://archive.apache.org/ "
    list "$2"                        # starting list function
    ;;
  "help")
    echo -e "This script easily can download and install Apache Maven or Ant packages.\\n
    There are list of arguments you can use:\\n
        install - install specify Maven or Ant version
        list - shows you list of available versions of Maven or ant
        remove - remove chosed Maven or Ant version
        use - you can chose what version do you want to use now ( only CentOS 7+ )
        help - user manual\\n
    Thank you for using script.\\n"
    ;;
  *)
    echo "\"$1\" no such argument. User \"help\" to see list of available arguments"
esac


