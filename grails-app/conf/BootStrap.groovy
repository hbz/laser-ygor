import grails.util.Environment

class BootStrap {

  def grailsApplication

  def init = { servletContext ->

    log.info('''

  ▓██   ██▓  ▄████  ▒█████   ██▀███  
   ▒██  ██▒ ██▒ ▀█▒▒██▒  ██▒▓██ ▒ ██▒
    ▒██ ██░▒██░▄▄▄░▒██░  ██▒▓██ ░▄█ ▒
    ░ ▐██▓░░▓█  ██▓▒██   ██░▒██▀▀█▄  
    ░ ██▒▓░░▒▓███▀▒░ ████▓▒░░██▓ ▒██▒
     ██▒ ▒  ░▒   ▒ ░ ▒░▒░▒░ ░ ▒▓ ░▒▓░
   ▓██ ░░   ░   ░   ░ ▒  ░    ░ ░ ░
   ░░                     
                       Yes, Master?
			''')
    log.info('Environment: ' + Environment.current)

  }

  def destroy = {
    log.info('I\'ll leave')
  }
}
