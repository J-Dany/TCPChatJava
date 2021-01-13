@echo off
git add .
git rm Server/.classpath
git rm Server/.project
git rm Client/.classpath
git rm Client/.project
git commit -m %1%
git push