#!/usr/bin/env python2

import sys

from funcsigs import signature


def getfrom(mod, name):
  try:
    return getattr(mod, name)
  except AttributeError:
    return mod.__dict__[name]


def main():
  target = sys.argv[1]
  if '.' in target:
    comps = target.split('.')
    mod_name = '.'.join(comps[:-1])
    mod = __import__(mod_name)
    mod = sys.modules[mod_name]
    obj = getfrom(mod, comps[-1])
  else:
    mod = None
    obj = getfrom(__builtins__, target)

  doc = ''
  doc_ = obj.__doc__
  if doc_ is None:
    try:
      doc_ = obj.__init__.__doc__
    except NameError:
      try:
        doc_ = obj.__call__.__doc__
      except NameError:
        doc_ = None
  if doc_ is not None:
    doc = ' - %s' % (doc_.splitlines()[0],)
  print '%s%s%s' % (target, str(signature(obj)).strip(), doc)


if __name__ == '__main__':
  main()


