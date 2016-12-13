#!/usr/bin/perl
use warnings;
use strict;

use XML::XPath;
use XML::XPath::XMLParser;


#1. Get <Entity>
#2. Parse <Entity>
#	2.1. Collectible?		<Tag enumID="321" name="Collectible" type="Bool" value="1">
#		2.1.1. Next unless value is true
#   2.2. Hero?				<Tag enumID="202" name="CardType" type="CardType" value="3"/>
#       2.2.1. Next if value == 3
#   2.2. We need to get:
#		2.2.1. Cost			<Tag enumID="48" name="Cost" type="Int" value="5"/>
#		2.2.2. Name			<Tag enumID="185" name="CardName" type="LocString"><enUS>Flame Lance</enUS></Tag>
#		2.2.3. Class		<Tag enumID="199" name="Class" type="Class" value="4"/>
#		2.2.4. Rarity		<Tag enumID="203" name="Rarity" type="Rarity" value="1"/>
#		2.2.5. CardSet		<Tag enumID="183" name="CardSet" type="CardSet" value="15"/>
#		2.2.6. CardID		<Entity CardID="AT_001" version="2">
#		2.2.7. CardType		<Tag enumID="202" name="CardType" type="CardType" value="5"/>
#3. Save to CardDB.txt

#1. Get <Entity>
my $entities_ref = getEntities();
#2. Parse <Entity>
$entities_ref = keepCollectible($entities_ref);
#3. Save to CardDB.txt
saveToTxt($entities_ref);
print "Done\n";

sub saveToTxt {
	#title of each column
	open FH, "> CardDB.txt" or die $!;
	print FH "Cost\tName\tClass\tRarity\tCardSet\tCardID\tCardType\n";
	# 2.1.6. save the temp file
	my $cards_ref = shift;
	for my $card_ref (@{$cards_ref}) {
		print FH $card_ref->{"Cost"},		"\t";
		print FH $card_ref->{"CardName"},	"\t";
		print FH $card_ref->{"Class"},		"\t";
		print FH $card_ref->{"Rarity"},		"\t";
		print FH $card_ref->{"CardSet"},	"\t";
		print FH $card_ref->{"CardID"},		"\t";
		print FH $card_ref->{"CardType"},	"\n";
	}
	close FH;
}

sub keepCollectible {
	my $entities_ref = shift;
	my @entities;
	
	for my $anEntity (@{$entities_ref}) {
		my $xp = XML::XPath->new(xml => $anEntity);
		#2.1. Collectible?
		next unless $xp->findvalue('/Entity/Tag[enumID="321"]/@value');
		#2.2. Hero?
		my $CardType = $xp->findvalue('/Entity/Tag[enumID="202"]/@value');
		next if $CardType == 3;
		#2.3. We need to get:
		my %card = (
			Cost		=> $xp->findvalue('/Entity/Tag[enumID="48"]/@value'),
			CardName	=> $xp->findvalue('/Entity/Tag[enumID="185"]/enUS'),
			Class		=> changeValue2Class($xp->findvalue('/Entity/Tag[enumID="199"]/@value')),
			Rarity		=> changeValue2Rarity($xp->findvalue('/Entity/Tag[enumID="203"]/@value')),
			CardSet		=> $xp->findvalue('/Entity/Tag[enumID="183"]/@value'),
			CardID		=> $xp->findvalue('/Entity/@CardID'),
			CardType	=> changeValue2CardType($CardType)
		);
		push @entities, \%card;
	}

	return \@entities;
}

sub changeValue2Class {
	my $value = shift;
	my $text;
	
	if		($value == 2) { $text = "Druid";}
	elsif	($value == 3) { $text = "Hunter";}
	elsif	($value == 4) { $text = "Mage";}
	elsif	($value == 5) { $text = "Paladin";}
	elsif	($value == 6) { $text = "Priest";}
	elsif	($value == 7) { $text = "Rogue";}
	elsif	($value == 8) { $text = "Shaman";}
	elsif	($value == 9) { $text = "Warlock";}
	elsif	($value == 10){ $text = "Warrior";}
	elsif	($value == 12){ $text = "Neutral";}
	
	return checkDebugMode($value, $text);
}

sub changeValue2Rarity {
	my $value = shift;
	my $text;
	
	if		($value == 1) { $text = "Common";}
	elsif	($value == 2) { $text = "Basic";}
	elsif	($value == 3) { $text = "Rare";}
	elsif	($value == 4) { $text = "Epic";}
	elsif	($value == 5) { $text = "Legendary";}
	
	return checkDebugMode($value, $text);
}

sub changeValue2CardType {
	my $value = shift;
	my $text;
	
	if		($value == 4) { $text = "Minion";}
	elsif	($value == 5) { $text = "Spell";}
	elsif	($value == 7) { $text = "Weapon";}
	elsif	($value == 3) { $text = "Hero";}
	
	return checkDebugMode($value, $text);
}

sub checkDebugMode {
	my $value = shift;
	my $text = shift;
	
	return "$value$text";
}

sub getEntities {
	my @entities;
	my $xp = XML::XPath->new(filename => $ARGV[0]);
	my $nodeset = $xp->find("/CardDefs/Entity");
 	for my $nodes ($nodeset->get_nodelist) {
		my $node = XML::XPath::XMLParser::as_string($nodes);
		push @entities, $node;
	}
	return \@entities;
}
