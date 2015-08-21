import sys;
import argparse;
import saife;
import json;
import time;
import threading;

# Parse the command line arguments

parser = argparse.ArgumentParser(description='This is a demo script by nixCraft.')
parser.add_argument('-msg','--message', nargs='+', help='<Required> Set flag', required=False)
parser.add_argument('-sess','--session', nargs='+', help='<Required> Set flag', required=False)
parser.add_argument('-c','--server',help='Sever Name', required=False)

args = parser.parse_args()

# Just some stuff that needs to be set

defaultKeyStore = "SaifeStore"
defaultPassword = "apassword"
updateSaifeInterval = 3600
CONST_NAME = "com.saife.demo.echo"
dn = saife.DistinguishedName("pyEchoTest")
APP_CAPS= "com::saife::demo::echo"


# This is a defintion for a thread that will update the saife date

def updateSaifeData():
    try:
        saifeptr.UpdateSaifeData()
        interval = updateSaifeInterval # Revert to default update interval after a successful udate
    except saife.IOException as e:
        interval = 30 # Update often after a failure done
        print e.error()
    t = threading.Timer(interval, updateSaifeData) #Interval is in seconds
    t.setDaemon(True)
    t.start()


def runMessageClient():
	while True: 
		for i in args.message: 
			bytes = map(ord, i)
			saifeptr.SendMessage(bytes,str(CONST_NAME), contact, 30, 2000, False)
			print "Msg >: " + "'" + i + "'"
			time.sleep(0.5)
		print  "Ok .. All done.  Sent  %i messages" % (len(args.message))


def runMessageServer():
	while True:
		try:
			messages = saife.SaifeMessageDataVector()
			saifeptr.GetMessages(str(CONST_NAME), messages)
			for msg in messages:
					messageAlais = msg.sender.alias()
					messageString = "".join("%c" % b for b in msg.message_bytes)
					print  "M:" + messageAlais + " '" + messageString + "'"
		except:
			print "bad or no messages"
		time.sleep(1)



def runSessionClient():
	saifeptr.EnablePresence();
	while True:
		time.sleep(1)
		session = saifeptr.ConstructSecureSession();
		print "session made"
		# lossssy = saife.SaifeSecureSessionInterface.TransportType.LOSSY
		lossssy = saife.SaifeSecureSessionInterface.LOSSY
		print lossssy
		try:
			session.Connect(contact, lossssy , 10)

			for i in args.session:
				print i
				try:
					time.sleep(1)
					bbytes = map(ord, i)
					print bbytes
					session.Write(bbytes)
				except:
					print "could not send"
			time.sleep(3)
			session.Close()
			saifeptr.ReleaseSecureSession(session)

		except saife.IOException as e:
			# print e.error()
			print "error"
	time.sleep(2)


#  Work in progress need to parse messages correctly
def runSessionServer():
	saifeptr.EnablePresence();
	print "Server Set Up"
	while True:
		try:
			session = saifeptr.ConstructSecureSession();
			session = saifeptr.Accept()
			
			peer = saife.SaifeContactVector()
			peer = session.GetPeer()
			print "Hey ..." + peer.alias() + " Just Connected"
			try:
				
				messagearray = [1]
				for i in messagearray:
					message = saife.UInt8Vector()
					session.Read(message, 1024, 30000)
					holder = ''.join(map(str,message))
					print message
					print  holder
					print message.capacity()

			except:
				print "error1"
		except:
			print "error2"
			
		time.sleep(1)	



# Create instance of SAIFE. A log manager may be optionally specified to redirect SAIFE logging.


saifeFactory = saife.SaifeFactory();
saifeptr = saifeFactory.ConstructLocalSaife(None);
saifeptr.SetSaifeLogLevel(saife.LogSinkInterface.SAIFE_LOG_WARNING)



state = saifeptr.Initialize(defaultKeyStore);
if state == saife.SAIFE_UNKEYED:
	print "Keying Up";
	CertificateSigningRequest = saife.CertificateSigningRequest();
	saifeptr.GenerateSmCsr(dn, defaultPassword, saife.SaifeAddressVector(), CertificateSigningRequest);
	print CertificateSigningRequest.csr();
	
	appCaps = saife.StringVector(CertificateSigningRequest.capabilities())
	appCaps.append(APP_CAPS)
	CertificateSigningRequest.set_capabilities(appCaps)
	caps = CertificateSigningRequest.capabilities()
	print json.dumps(caps);
else:
	# Unlock
	saifeptr.Unlock(defaultPassword)
	# Update Data
	saifeptr.UpdateSaifeData()
	time.sleep(2)
	# subscribe
	saifeptr.Subscribe()
	# sync contacts
	saifeptr.SynchronizeContacts()
	# setup update thread
	updateSaifeData()

	if args.server:		
		# just give a little time just to make sure everything is set up
		time.sleep(2)
		contact = saife.SaifeContactVector()
		contactholder = saifeptr.GetContactByAlias(args.server)
		contact = contactholder

	if args.message:
		runMessageClient()
	elif args.session:
		runSessionClient()
	else:
		runMessageServer()
