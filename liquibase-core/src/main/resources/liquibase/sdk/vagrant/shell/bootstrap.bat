echo Copying vagrant-install-files...
mkdir c:\install

echo Installing puppet librarian
call "C:\Program Files (x86)\Puppet Labs\Puppet\bin\environment.bat"
mkdir c:\usr\share\puppet
cd \usr\share\puppet
copy \vagrant\Puppetfile .

if not exist c:\usr\share\puppet\.librarian (
    call gem install librarian-puppet --no-rdoc --no-ri
    call librarian-puppet install
)

echo Running puppet.....
call puppet apply -vv  --color=false --modulepath=c:\usr\share\puppet\modules\;c:\vagrant\modules c:\vagrant\manifests\init.pp

echo Finished bootstrap.bat