#!/usr/bin/env python2

import sys

from funcsigs import signature


def main():
  target = sys.argv[1]
  if '.' in target:
    comps = target.split('.')
    mod = '.'.join(comps[:-1])
    mod = __import__(mod)
    obj = getattr(mod, comps[-1])
  else:
    mod = None
    obj = getattr(__builtins__, target)

  print '%s%s' % (target, str(signature(obj)))


if __name__ == '__main__':
  main()


