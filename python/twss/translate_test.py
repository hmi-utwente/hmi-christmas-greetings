from __future__ import print_function
from googleapiclient.discovery import build


def main():
  # Build a service object for interacting with the API. Visit
  # the Google APIs Console <http://code.google.com/apis/console>
  # to get an API key for your own application.
  service = build('translate', 'v2',
            developerKey='AIzaSyBkhnyE-dN-GpANsLbzDb7csP2F-M2Br_c')
  res = service.translations().list(
      source='nl',
      target='en',
      q=['Ik heb een mooi kleur']
    ).execute()
  print(res['translations'][0]['translatedText'])

if __name__ == '__main__':
  main()
