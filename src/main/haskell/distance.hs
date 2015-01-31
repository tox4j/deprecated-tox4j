import Data.Bits
import Numeric


kadDist :: Integer -> Integer -> Integer
kadDist a b = a `xor` b


toxDist :: Integer -> Integer -> Integer
toxDist 0 0 = 0
toxDist a b =
  let
    -- rightmost bytes of a and b
    byteA = a .&. 0xff
    byteB = b .&. 0xff
    -- rest of a and b, left of the above bytes
    a' = a `shiftR` 8
    b' = b `shiftR` 8
  in
  dist1 byteA byteB .|. toxDist a' b' `shiftL` 8
  where
    int8_t x =
      if x >= 128 then
        -(256 - x)
      else
        x
    dist1 byteA byteB =
      abs $ (int8_t byteA) `xor` (int8_t byteB)


main =
  let
    a = 0x04119E835DF3E78BACF0F84235B300546AF8B936F035185E2A8E9E0A67C8924F
    b = 0xA09162D68618E742FFBCA1C2C70385E6679604B2D80EA6E84AD0996A1AC8A074
  in do
  () <- putStrLn $ (showHex $ kadDist a b) ""
  () <- putStrLn $ (showHex $ toxDist a b) ""
  return ()
