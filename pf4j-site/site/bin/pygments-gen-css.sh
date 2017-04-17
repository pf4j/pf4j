#! /bin/bash
if [ "$1" == "" ]
then
    echo -e "\nSpecify the style please:\n"
    #locate pygment | grep --color style
    ls -l /Library/Ruby/Gems/1.8/gems/pygments.rb-0.5.0/vendor/pygments-main/pygments/styles
else
    pygmentize -S $1 -f html -a .highlight 
fi
