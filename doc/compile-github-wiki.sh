#！/bin/bash
amwiki -u
amwiki -e github-wiki ../../wiki
cd ../../wiki
git add *
git commit -m "Update documents"
git push
