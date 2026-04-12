@echo off
"C:\Program Files\GitHub CLI\gh.exe" label create PM --repo xiaoguo-jlu/open-tms --color "1D76DB" --description "Product Manager"
"C:\Program Files\GitHub CLI\gh.exe" label create TA --repo xiaoguo-jlu/open-tms --color "F88" --description "Technical Architect"
"C:\Program Files\GitHub CLI\gh.exe" label create Dev --repo xiaoguo-jlu/open-tms --color "84B6EB" --description "Developer"
"C:\Program Files\GitHub CLI\gh.exe" label create QA --repo xiaoguo-jlu/open-tms --color "E4E669" --description "QA Engineer"
"C:\Program Files\GitHub CLI\gh.exe" label create UX --repo xiaoguo-jlu/open-tms --color "D4C5F9" --description "UX Designer"
"C:\Program Files\GitHub CLI\gh.exe" label create PM-Lead --repo xiaoguo-jlu/open-tms --color "0E8A16" --description "Project Lead"
"C:\Program Files\GitHub CLI\gh.exe" label create Feature --repo xiaoguo-jlu/open-tms --color "0E8A16" --description "New Feature"
"C:\Program Files\GitHub CLI\gh.exe" label create US --repo xiaoguo-jlu/open-tms --color "D4C5F9" --description "User Story"
"C:\Program Files\GitHub CLI\gh.exe" label create Task --repo xiaoguo-jlu/open-tms --color "3D4F9F" --description "Technical Task"
"C:\Program Files\GitHub CLI\gh.exe" label create Bug --repo xiaoguo-jlu/open-tms --color "CB2431" --description "Bug"
"C:\Program Files\GitHub CLI\gh.exe" label create "技术债务" --repo xiaoguo-jlu/open-tms --color "FBCA04" --description "Technical Debt"
echo Done